# mall-cloud 认证中心学习笔记

---

# 第一部分：mall-auth 模块完整梳理

## 一、模块定位

`mall-auth` 是 mall-cloud 微服务电商平台的 **统一认证中心**，负责处理后台管理员和前台会员的登录请求。它本身不存储用户数据，而是作为认证路由层——根据 `clientId` 区分请求来源，将登录请求转发到对应的微服务（`user-service` 处理管理员登录，`member-service` 处理会员登录），实现统一入口、分散认证的架构。

---

## 二、技术架构全景

```
        客户端 (后台管理 / 前台商城)
                     │
                     v
              ┌──────────────┐
              │  mall-gateway  │  ← 网关（白名单放行 /mall-auth/**）
              └──────┬───────┘
                     │
                     v
          ┌──────────────────────────┐
          │     mall-auth :8401      │
          │      (统一认证中心)        │
          │                          │
          │  AuthController          │
          │  POST /auth/login        │
          │  ┌────────────────────┐  │
          │  │ clientId="admin-app"│  │
          │  │      ↓              │  │
          │  │  UserClient.login() │──┼──→ user-service /admin/login
          │  │      ↓              │  │     (后台管理员 JWT Token)
          │  │  返回 token          │  │
          │  └────────────────────┘  │
          │  ┌────────────────────┐  │
          │  │ clientId="portal-app"│ │
          │  │      ↓              │  │
          │  │ MemberClient.login()│──┼──→ member-service /sso/login
          │  │      ↓              │  │     (前台会员 JWT Token)
          │  │  返回 token          │  │
          │  └────────────────────┘  │
          └──────────────────────────┘
```

---

## 三、目录结构

```
mall-auth/
├── pom.xml
└── src/main/
    ├── java/com/mym/mall/auth/
    │   ├── MallAuthApplication.java        # 启动类
    │   ├── config/
    │   │   └── SpringDocConfig.java        # API 文档配置
    │   └── controller/
    │       └── AuthController.java          # 统一认证控制器 ★
    └── resources/
        ├── application.yml                 # 主配置
        ├── application-dev.yml             # 开发环境
        └── application-prod.yml            # 生产环境
```

---

## 四、核心依赖解读（pom.xml）

| 依赖 | 作用 |
|------|------|
| `mall-common` | 公共模块（排除 `spring-boot-starter-data-redis`） |
| `mall-api` | Feign 接口模块，提供 `UserClient` 和 `MemberClient` |
| `spring-boot-starter-web` | Web 服务 |
| `spring-cloud-starter-alibaba-nacos-discovery` | Nacos 服务注册发现 |
| `spring-cloud-starter-alibaba-nacos-config` | Nacos 配置中心 |
| `spring-cloud-starter-openfeign` | OpenFeign 跨服务调用 |
| `spring-boot-admin-starter-client` | Spring Boot Admin 监控客户端 |

**关键设计点**：mall-auth 排除了 `spring-boot-starter-data-redis`，因为它是纯路由层，不需要直接操作 Redis。认证的状态管理由 `user-service` 和 `member-service` 自行处理。

---

## 五、核心代码详解

### 5.1 启动类

```java
@EnableFeignClients(basePackages = "com.mall.api.client")  // 扫描 mall-api 中的 Feign 接口
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.mym.mall")
public class MallAuthApplication { ... }
```

**注意**：`@EnableFeignClients` 扫描的包是 `com.mall.api.client`（mall-api 模块），`@SpringBootApplication` 扫描的包是 `com.mym.mall`（本项目代码）。这是典型的"跨模块代码组织"——Feign 接口定义在 mall-api，业务实现在 mall-auth。

### 5.2 AuthController — 认证路由核心

```java
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserClient userClient;      // → user-service
    private final MemberClient memberClient;   // → member-service

    @PostMapping("/login")
    public CommonResult<Map<String, String>> login(
            @RequestParam String clientId,
            @RequestParam String username,
            @RequestParam String password) {

        if (AuthConstant.ADMIN_CLIENT_ID.equals(clientId)) {
            // 后台管理员登录
            Map<String, String> params = new HashMap<>();
            params.put("username", username);
            params.put("password", password);
            return userClient.login(params);
        } else if (AuthConstant.PORTAL_CLIENT_ID.equals(clientId)) {
            // 前台会员登录
            return memberClient.login(username, password);
        } else {
            return CommonResult.failed("clientId不正确");
        }
    }
}
```

### 5.3 认证路由决策树

```
POST /auth/login
    │
    ├─ clientId == "admin-app"？
    │      └─ YES → UserClient.login(params)
    │              → Feign → user-service /admin/login
    │              → 返回 JWT Token
    │
    ├─ clientId == "portal-app"？
    │      └─ YES → MemberClient.login(username, password)
    │              → Feign → member-service /sso/login
    │              → 返回 JWT Token
    │
    └─ 其他 clientId
           └─ 返回 "clientId不正确"
```

### 5.4 UserClient — 管理员登录 Feign 接口

```java
@FeignClient(name = "user-service", path = "/admin")
public interface UserClient {
    @GetMapping("/{id}")
    CommonResult<?> getById(@PathVariable Long id);

    @GetMapping("/loadByUsername")
    CommonResult<?> loadByUsername(@RequestParam String username);

    @PostMapping("/login")
    CommonResult<Map<String, String>> login(@RequestBody Map<String, String> params);
}
```

### 5.5 MemberClient — 会员登录 Feign 接口

```java
@FeignClient(name = "member-service")
public interface MemberClient {
    @GetMapping("/member/{id}")
    CommonResult<MemberDTO> getById(@PathVariable Long id);

    @PostMapping("/sso/login")
    CommonResult<Map<String, String>> login(
        @RequestParam String username,
        @RequestParam String password
    );
}
```

### 5.6 SpringDoc API 文档配置

```java
@Configuration
public class SpringDocConfig implements WebMvcConfigurer {
    @Bean
    public OpenAPI mallAuthOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("mall认证中心")
                    .description("mall认证中心相关接口文档")
                    .version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .components(new Components()
                    .addSecuritySchemes("Authorization",
                        new SecurityScheme()
                            .name("Authorization")
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")));
    }
}
```

---

## 六、配置文件详解

### 6.1 主配置

```yaml
server:
  port: 8401                      # 认证中心端口
spring:
  application:
    name: mall-auth
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher  # 兼容 Swagger 路径匹配
management:
  endpoints:
    web:
      exposure:
        include: '*'              # 暴露所有 actuator 端点（给 Admin 监控）
    endpoint:
      health:
        show-details: always
      env:
        show-values: always       # 环境变量明文显示
      configprops:
        show-values: always       # 配置属性明文显示
```

### 6.2 多环境配置

| 环境 | Nacos 地址 | Nacos 配置文件 |
|------|-----------|---------------|
| dev | `localhost:8848` | `mall-auth-dev.yaml` |
| prod | `nacos-registry:8848` | `mall-auth-prod.yaml` |

---

## 七、认证请求完整数据流

```
1. 用户发起登录
   请求: POST /auth/login
   参数: clientId=admin-app, username=admin, password=123456

2. mall-auth 处理
   AuthController 判断 clientId = "admin-app"

3. Feign 调用转发
   UserClient.login({"username":"admin","password":"123456"})
   → HTTP POST http://user-service/admin/login

4. user-service 认证
   → 查询数据库验证用户名密码
   → Sa-Token 生成 Token
   → 返回 {"code":200, "data":{"token":"xxxxx"}}

5. mall-auth 透传响应
   → 将 user-service 的响应原样返回给客户端

6. 客户端收到 Token
   → 后续请求在 Header 中带上 Authorization: Bearer xxxxx
   → 网关 SaReactorFilter 校验 Token 有效性
```

---

## 八、核心设计亮点总结

1. **路由层而非认证层**：mall-auth 不进行实际的密码校验，只做请求路由，职责单一
2. **clientId 区分**：通过 `clientId` 参数实现后台/前台双通道，便于客户端切换登录模式
3. **Feign 透传**：认证中心的接口直接透传 Feign 调用的返回，不二次加工
4. **零状态**：mall-auth 不存任何 Session/Token，不操作 Redis，天然支持水平扩展
5. **Nacos 配置热更新**：通过 `refreshEnabled=true` 支持运行时配置变更
6. **安全文档**：API 文档集成了 JWT Bearer Token 认证方案

---

---

# 第二部分：Java 面试认证模块高频问题

## 面试题 1：为什么需要一个独立的认证中心？

**单体架构**：每个请求都经过同一个后端，直接在 Controller 里校验即可。

**微服务架构**：认证分散在各服务会导致三个问题：
1. **代码重复**：每个服务都要写一遍认证逻辑
2. **Token 不一致**：不同服务生成的 Token 格式不同
3. **统一登录**：用户只需要登录一次

**mall-auth 的解决方案**：
- 作为统一入口，所有登录请求都经过认证中心
- 认证中心不执行实际校验，而是路由到对应的业务服务
- 网关层统一校验 Token，认证中心与网关解耦

---

## 面试题 2：clientId 设计的目的是什么？

这是一个**多端入口识别**设计：

| clientId | 说明 | 登录目标 |
|----------|------|----------|
| `admin-app` | 后台管理系统 | user-service（管理员） |
| `portal-app` | 前台商城 | member-service（会员） |

**为什么需要 clientId**：
- 管理员和会员使用不同的账户体系
- 管理员需要 RBAC 权限控制，会员只需要登录状态
- 各自的 Token 格式、有效期、存储方式可能不同
- 方便后续扩展更多客户端（如商家端、小哥端）

**面试回答建议**：
> `clientId` 是多端入口的标识，让同一个 `/auth/login` 接口能服务后台管理员和前台会员两种不同的认证体系。管理员和会员是独立的账户数据表，生成 Token 的策略也不同（管理员用 Redis Session，会员用 JWT），通过 clientId 路由到不同的微服务处理。

---

## 面试题 3：认证中心如何实现无状态？

mall-auth 的无状态体现在：
1. **不做密码校验**：直接转发到 user-service/member-service
2. **不存储 Session**：不在本地内存或 Redis 中存任何认证状态
3. **不生成 Token**：Token 由下游服务生成
4. **不做权限判断**：权限校验在网关层完成

这使得 mall-auth 可以随意水平扩展，不需要考虑 Session 同步问题。

---

## 面试题 4：Nacos 配置热更新 `refreshEnabled=true` 的作用？

```yaml
spring:
  config:
    import:
      - nacos:mall-auth-dev.yaml?refreshEnabled=true
```

当 Nacos 上的 `mall-auth-dev.yaml` 配置内容变更时：
1. Nacos 推送变更通知给 mall-auth 服务
2. `@RefreshScope` 标注的 Bean 会被重新初始化
3. `@Value` 注解的属性值会被刷新
4. **无需重启服务**

**适用场景**：开关类配置（功能开关、限流阈值）、外部服务地址、日志级别等。

---

## 面试速查表

| 问题 | 一句话答案 |
|------|-----------|
| mall-auth 角色 | 认证路由层，不存状态，只转发请求 |
| clientId | 区分 admin-app(后台) 和 portal-app(前台) |
| Feign 调用 | UserClient → user-service，MemberClient → member-service |
| 无状态设计 | 不存 Session/Token，水平扩展友好 |
| 配置热更新 | `refreshEnabled=true` + Nacos 推送 |
| 安全文档 | Bearer JWT 认证方案的 OpenAPI 文档 |

---

*学习日期：2026-07-08*
