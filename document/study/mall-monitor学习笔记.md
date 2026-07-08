# mall-cloud 监控模块学习笔记

---

# 第一部分：mall-monitor 模块完整梳理

## 一、模块定位

`mall-monitor` 是 mall-cloud 微服务电商平台的 **监控服务端**，基于 **Spring Boot Admin Server** 构建。它自动发现注册在 Nacos 上的所有微服务实例，提供统一的健康检查、性能指标、日志查看、环境变量等监控能力。配合 Spring Security 保护管理界面，防止未授权访问。

---

## 二、技术架构全景

```
                     运维人员浏览器
                           │
                           v
               ┌─────────────────────────┐
               │     Spring Security      │
               │  (Form Login + HTTP Basic)│
               └───────────┬─────────────┘
                           │ 认证通过
                           v
               ┌─────────────────────────┐
               │   Spring Boot Admin     │
               │   Server (端口 8101)     │
               │                         │
               │  ┌───────────────────┐  │
               │  │  服务自动发现       │  │ ← Nacos Discovery
               │  │  (Cloud Discovery) │  │
               │  └───────────────────┘  │
               └─────────────────────────┘
                    │    │    │    │
               ┌────┘    │    │    └────┐
               v         v    v         v
         mall-gateway  mall-auth  product-service  ...
         (actuator)   (actuator)  (actuator)
```

**关键组件**：
- **Spring Boot Admin Server**：监控仪表盘，聚合展示所有微服务状态
- **Nacos Discovery**：自动发现注册中心中的所有服务实例
- **Spring Security**：保护 Admin 控制台，需登录才能访问
- **CSRF 防护**：通过 Cookie (`XSRF-TOKEN`) 传递 CSRF Token

---

## 三、目录结构

```
mall-monitor/
├── pom.xml
└── src/main/
    ├── java/com/mym/mall/
    │   ├── MallMonitorApplication.java       # 启动类 ★
    │   ├── config/
    │   │   └── SecuritySecureConfig.java     # Security 安全配置 ★★
    │   └── filter/
    │       └── CustomCsrfFilter.java         # 自定义 CSRF 过滤器
    └── resources/
        ├── application.yml                   # 主配置
        ├── application-dev.yml               # 开发环境（Nacos localhost）
        └── application-prod.yml              # 生产环境（Nacos 容器名）
```

---

## 四、核心依赖解读（pom.xml）

| 依赖 | 作用 |
|------|------|
| `spring-boot-starter-web` | Web 容器，运行 Admin Server 的 HTTP 服务 |
| `spring-cloud-starter-alibaba-nacos-discovery` | 集成 Nacos 服务发现，自动拉取服务列表 |
| `spring-boot-admin-starter-server` | **Spring Boot Admin 服务端核心**，提供监控仪表盘 |
| `spring-boot-starter-security` | 安全认证，保护 Admin 控制台不被未授权访问 |

**关键设计点**：monitor 模块不需要 `mall-common`，也不注册 Feign 客户端。它的职责单一纯粹——只做监控，不参与业务调用链。

---

## 五、启动类详解

```java
@EnableDiscoveryClient    // 注册到 Nacos，并自动发现其他服务
@EnableAdminServer        // 启用 Spring Boot Admin Server
@SpringBootApplication
public class MallMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallMonitorApplication.class, args);
    }
}
```

`@EnableAdminServer` 是模块的核心注解，它将当前应用转变为一个 Admin 服务端。所有添加了 `spring-boot-admin-starter-client` 的微服务会自动注册到它。

---

## 六、Security 安全配置详解

### 6.1 认证流程

```
浏览器请求 Admin 仪表盘
     │
     ▼
Spring Security Filter Chain
     │
     ├─ 请求 /assets/**、/login、/actuator/* ？
     │      └─ YES → 直接放行
     │
     ├─ 其他请求 → 要求认证
     │      └─ 未登录 → 302 重定向到 /login
     │      └─ 已登录 → 放行
     │
     ▼
CustomCsrfFilter → 写入 XSRF-TOKEN Cookie
     │
     ▼
返回 Admin 仪表盘
```

### 6.2 SecuritySecureConfig 核心配置

```java
@Bean
protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/assets/**").permitAll()       // 静态资源
            .requestMatchers("/actuator/info").permitAll()   // 健康信息
            .requestMatchers("/actuator/health").permitAll() // 健康检查
            .requestMatchers("/login").permitAll()           // 登录页
            .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll() // 异步请求
            .anyRequest().authenticated()                    // 其余需认证
        )
        .formLogin(form -> form
            .loginPage("/login")                             // 自定义登录页
            .successHandler(successHandler))                 // 登录成功跳转
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // Cookie 存 CSRF
            .ignoringRequestMatchers(
                "/instances", "/instances/*", "/actuator/**")) // 忽略 CSRF 的路径
        .rememberMe(remember -> remember
            .key(UUID.randomUUID().toString())
            .tokenValiditySeconds(1209600));    // 14 天
    return http.build();
}

@Bean
public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
    UserDetails user = User.withUsername("macro")
            .password(encoder.encode("123456"))
            .roles("USER")
            .build();
    return new InMemoryUserDetailsManager(user);
}
```

### 6.3 安全配置要点

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 默认用户名 | `macro` | 内存用户 |
| 默认密码 | `123456` | BCrypt 加密存储 |
| 角色 | `USER` | 基础角色 |
| Remember Me | 14 天 | token 有效期 |
| CSRF 存储 | Cookie `XSRF-TOKEN` | 非 HttpOnly，前端可读 |

### 6.4 CustomCsrfFilter 详解

```java
public class CustomCsrfFilter extends OncePerRequestFilter {
    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain) {
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            Cookie cookie = WebUtils.getCookie(request, CSRF_COOKIE_NAME);
            String token = csrf.getToken();
            // Cookie 不存在或 Token 不匹配时写入新 Cookie
            if (cookie == null || token != null && !token.equals(cookie.getValue())) {
                cookie = new Cookie(CSRF_COOKIE_NAME, token);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
        chain.doFilter(request, response);
    }
}
```

**为什么需要自定义 CSRF 过滤器**：Spring Boot Admin 的前端通过 Cookie 读取 CSRF Token（而非从 HTML 页面的 `<meta>` 标签），标准 Spring Security 的 CSRF Token 存储在请求属性中，前端无法直接访问。`CustomCsrfFilter` 将 Token 写入 Cookie，使 Admin 前端能够获取。

---

## 七、配置文件详解

### 7.1 主配置

```yaml
server:
  port: 8101                              # Admin Server 端口
spring:
  application:
    name: mall-monitor
  boot:
    admin:
      discovery:
        ignored-services: mall-monitor    # ★ 不监控自己
```

`ignored-services: mall-monitor` 确保 Admin Server 自身不出现在监控列表中，只展示被监控的微服务。

### 7.2 多环境配置

| 环境 | Nacos 地址 | 特殊配置 |
|------|-----------|---------|
| dev | `localhost:8848` | - |
| prod | `nacos-registry:8848` | Docker 容器内部域名 |

---

## 八、监控数据流

```
被监控服务 (如 product-service)
    │
    │ 添加 spring-boot-admin-starter-client 依赖
    │ 暴露 actuator 端点: management.endpoints.web.exposure.include=*
    │
    ├──→ 注册到 Nacos
    │
    ▼
Nacos 注册中心
    │
    │ mall-monitor 通过 DiscoveryClient 发现所有服务
    │
    ▼
Spring Boot Admin Server (mall-monitor)
    │
    │ 定时拉取每个服务的 /actuator/health、/actuator/info、/actuator/metrics 等
    │
    ▼
Admin 仪表盘展示：
  ├─ 服务状态 (UP/DOWN)
  ├─ 内存使用
  ├─ GC 信息
  ├─ 线程数
  ├─ 请求指标
  ├─ 环境变量
  ├─ 日志查看
  └─ JVM 信息
```

---

## 九、核心设计亮点总结

1. **零配置服务发现**：通过 Nacos 自动发现服务，被监控服务只需添加 `spring-boot-admin-starter-client` 依赖即可
2. **自身隔离**：`ignored-services` 确保 Admin Server 不监控自己
3. **安全性保护**：Spring Security 保护管理界面，内存用户认证
4. **CSRF 兼容**：`CustomCsrfFilter` 将 CSRF Token 写入 Cookie，适配 Admin 前端
5. **多环境配置**：dev/prod 通过不同的 Nacos 地址和配置中心实现环境隔离
6. **全量端点暴露**：被监控服务暴露所有 actuator 端点（`include: '*'`），提供完整的监控数据

---

---

# 第二部分：Java 面试 Spring Boot Admin 高频问题

## 面试题 1：Spring Boot Admin 的原理是什么？

Spring Boot Admin 分为 Server 和 Client 两部分：

**Server（mall-monitor）**：
- 使用 `@EnableAdminServer` 启用
- 通过 `DiscoveryClient`（Nacos）自动发现所有注册的微服务
- 定时轮询各服务的 `/actuator` 端点获取监控数据
- 在 Web 仪表盘上聚合展示

**Client（各微服务）**：
- 添加 `spring-boot-admin-starter-client` 依赖
- 自动注册到 Admin Server
- 暴露 actuator 端点供 Server 拉取数据

**面试回答建议**：
> Spring Boot Admin 是一个监控管理系统，Server 端通过 `@EnableAdminServer` 启动仪表盘，Client 端通过 starter 自动注册。Server 通过两种方式发现服务：一是手动配置 URL，二是通过服务发现组件（Nacos/Eureka）自动发现。发现后 Server 定时拉取各服务的 `/actuator/health`、`/actuator/metrics` 等端点，聚合展示在仪表盘上。

---

## 面试题 2：actuator 都暴露了哪些端点？各有什么作用？

| 端点 | 作用 | 是否默认暴露 |
|------|------|-------------|
| `/actuator/health` | 健康检查 | ✅ |
| `/actuator/info` | 应用信息 | ✅ |
| `/actuator/metrics` | JVM、HTTP 指标 | ❌ |
| `/actuator/env` | 环境变量和配置 | ❌ |
| `/actuator/loggers` | 日志级别查看/修改 | ❌ |
| `/actuator/httpexchanges` | HTTP 请求追踪 | ❌ |
| `/actuator/threaddump` | 线程转储 | ❌ |
| `/actuator/heapdump` | 堆转储（下载 .hprof） | ❌ |
| `/actuator/beans` | Spring Bean 列表 | ❌ |
| `/actuator/mappings` | URL 映射列表 | ❌ |

**安全注意**：生产环境不应该暴露 `env`、`heapdump` 等敏感端点。

---

## 面试题 3：Spring Security 的 CSRF 保护在 Admin 中为什么需要自定义？

**标准 CSRF 流程**：Token 存储在 Session 中，通过 `<meta>` 标签或表单隐藏字段传递给前端。

**问题**：Spring Boot Admin 是单页应用（SPA），通过 JavaScript 发起 AJAX 请求，无法读取 HTML 中的 `<meta>` 标签。因此需要将 CSRF Token 写入 Cookie 供 JavaScript 读取。

**解决方案**：
```java
// 使用 Cookie 存储 CSRF Token（非 HttpOnly）
.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
// CustomCsrfFilter 确保 Cookie 和 Token 同步
```

**面试回答建议**：
> Spring Boot Admin 作为单页应用，前端 JavaScript 通过 AJAX 发送请求时需要 CSRF Token。标准方式中 Token 存在 Session 里，前端无法直接获取。项目中通过将 CSRF Token 存入 Cookie（`withHttpOnlyFalse()`），并在 CustomCsrfFilter 中保持 Cookie 与 Token 同步，让前端能从 Cookie 中读取 Token 并设置到请求头。

---

## 面试题 4：为什么 mall-monitor 不监控自己？

```yaml
spring:
  boot:
    admin:
      discovery:
        ignored-services: mall-monitor
```

这是一个 UX 优化：Admin Server 本身也是一个 actuator 端点暴露的服务，如果它也出现在监控列表中会造成混淆——"监控面板在监控监控面板"，没有实际意义。

---

## 面试题 5：如何在生产环境中安全地暴露 actuator 端点？

```yaml
# ❌ 危险做法
management:
  endpoints:
    web:
      exposure:
        include: '*'

# ✅ 推荐做法
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

**生产环境建议**：
1. 只暴露 `health`、`info`、`metrics` 等必要端点
2. 敏感端点（`env`、`heapdump`、`threaddump`）仅内部网络访问
3. 配合 Spring Security 做端点级别的权限控制
4. 通过 Kubernetes Probe 使用 `/actuator/health/liveness` 和 `/actuator/health/readiness`

---

## 面试速查表

| 问题 | 一句话答案 |
|------|-----------|
| SBA 原理 | Server 定时拉取 Client 的 /actuator 端点 |
| 服务发现 | 通过 Nacos Discovery 自动发现 + 注册 |
| CSRF 自定义 | Admin 前端读 Cookie ≠ 读 Session，需 CustomCsrfFilter |
| ignored-services | 让 Admin Server 不监控自己 |
| 安全建议 | 生产环境只暴露 health + info + metrics |
| 用户名密码 | macro / 123456 (BCrypt 加密) |
| Remember Me | 14 天有效期，UUID 随机 key |

---

*学习日期：2026-07-08*
