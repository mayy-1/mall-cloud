# mall-swarm 网关模块学习笔记

---

# 第一部分：mall-gateway 模块完整梳理

## 一、模块定位

`mall-gateway` 是 mall-swarm 微服务电商平台的 **统一入口网关**，基于 **Spring Cloud Gateway (WebFlux 响应式)** 构建。所有客户端请求首先到达网关，由网关完成鉴权、路由转发、跨域处理后，再分发到下游微服务。

---

## 二、技术架构全景

```
                        客户端 (浏览器/App/小程序)
                                  |
                                  v
                    ┌─────────────────────────┐
                    │    mall-gateway :8201    │
                    │   Spring Cloud Gateway   │
                    │      (WebFlux 响应式)     │
                    │                         │
                    │  ┌───────────────────┐  │
                    │  │  CorsWebFilter    │  │  ← 全局跨域
                    │  └────────┬──────────┘  │
                    │           v             │
                    │  ┌───────────────────┐  │
                    │  │  SaReactorFilter  │  │  ← 鉴权过滤器
                    │  │  (白名单→认证→鉴权) │  │
                    │  └────────┬──────────┘  │
                    │           v             │
                    │  ┌───────────────────┐  │
                    │  │   Route Predicate │  │  ← 路由断言匹配
                    │  │   /mall-admin/**  │  │
                    │  │   /mall-portal/** │  │
                    │  │   /mall-auth/**   │  │
                    │  │   /mall-search/** │  │
                    │  └────────┬──────────┘  │
                    │           v             │
                    │  ┌───────────────────┐  │
                    │  │  StripPrefix=1    │  │  ← 路径重写
                    │  └────────┬──────────┘  │
                    │           v             │
                    │  ┌───────────────────┐  │
                    │  │  Nacos 服务发现    │  │  ← lb:// 负载均衡
                    │  └───────────────────┘  │
                    └─────────────────────────┘
                         |        |        |
                    ┌────┘    ┌───┘    ┌───┴────┐
                    v         v        v        v
               mall-auth  mall-admin mall-portal mall-search
```

---

## 三、目录结构

```
mall-gateway/
├── pom.xml
└── src/main/
    ├── java/com/macro/mall/
    │   ├── MallGatewayApplication.java       # 启动类
    │   ├── config/
    │   │   ├── GlobalCorsConfig.java          # 全局跨域配置
    │   │   ├── IgnoreUrlsConfig.java          # 白名单配置
    │   │   ├── RedisConfig.java               # Redis 配置
    │   │   └── SaTokenConfig.java             # 核心鉴权配置 ★
    │   ├── component/
    │   │   └── StpInterfaceImpl.java          # 权限接口实现
    │   └── util/
    │       └── StpMemberUtil.java             # 前台会员认证工具类
    └── resources/
        ├── application.yml                    # 主配置（路由+白名单+鉴权）
        ├── application-dev.yml                # 开发环境（Nacos 配置）
        └── application-prod.yml               # 生产环境（Nacos 配置）
```

---

## 四、核心依赖解读（pom.xml）

| 依赖 | 作用 |
|------|------|
| `spring-cloud-starter-gateway-server-webflux` | Spring Cloud Gateway 核心，基于 WebFlux + Netty |
| `spring-cloud-starter-alibaba-nacos-discovery` | 集成 Nacos 服务发现，使 `lb://` 负载均衡可用 |
| `spring-cloud-starter-alibaba-nacos-config` | 集成 Nacos 配置中心，远程配置热更新 |
| `sa-token-reactor-spring-boot3-starter` | Sa-Token 的响应式版本（适配 WebFlux） |
| `sa-token-redis-jackson` | Sa-Token 使用 Redis 存储会话 |
| `sa-token-jwt` | Sa-Token 集成 JWT 模式 |
| `knife4j-gateway-spring-boot-starter` | Knife4j 网关聚合文档 |
| `spring-boot-starter-data-redis` | Redis 客户端访问 |
| `spring-boot-admin-starter-client` | 接入 Spring Boot Admin 监控 |

**关键设计点**：pom.xml 中显式排除了 `spring-boot-starter-web`，因为 Spring Cloud Gateway 基于 **WebFlux（响应式）**，使用 Netty 而非 Tomcat，与传统的 Servlet 容器（WebMVC）互斥。

```xml
<exclusions>
    <!-- 排除 WebMVC，网关基于 WebFlux -->
    <exclusion>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </exclusion>
</exclusions>
```

---

## 五、路由配置详解

### 5.1 四条路由规则

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: mall-auth              # 路由唯一标识
          uri: lb://mall-auth        # 负载均衡转发到 mall-auth 服务
          predicates:
            - Path=/mall-auth/**     # 路径断言：匹配 /mall-auth/ 开头的请求
          filters:
            - StripPrefix=1          # 去掉第一段路径前缀

        - id: mall-admin
          uri: lb://mall-admin
          predicates:
            - Path=/mall-admin/**
          filters:
            - StripPrefix=1

        - id: mall-portal
          uri: lb://mall-portal
          predicates:
            - Path=/mall-portal/**
          filters:
            - StripPrefix=1

        - id: mall-search
          uri: lb://mall-search
          predicates:
            - Path=/mall-search/**
          filters:
            - StripPrefix=1
```

### 5.2 路由转发流程示例

以 `/mall-admin/admin/login` 为例：

```
客户端请求:  GET /mall-admin/admin/login
                        |
                        v
            ① Path Predicate 匹配 /mall-admin/**
                        |
                        v
            ② 鉴权：在白名单中 /mall-admin/admin/login → 直接放行
                        |
                        v
            ③ StripPrefix=1：去掉 /mall-admin
               请求变为 →  /admin/login
                        |
                        v
            ④ 通过 Nacos 发现 mall-admin 服务实例
                        |
                        v
            ⑤ lb://mall-admin 负载均衡选择一个实例
                        |
                        v
            ⑥ 转发到 mall-admin 服务的 /admin/login 接口
```

---

## 六、鉴权体系详解（核心精华）

### 6.1 双账号体系

该项目实现了 **管理员 + 会员** 两套独立的认证体系：

| 维度 | 管理员 | 前台会员 |
|------|--------|----------|
| 认证工具 | `StpUtil`（Sa-Token 默认） | `StpMemberUtil`（自定义） |
| 账号类型 (TYPE) | `"login"` | `"memberLogin"` |
| Token 模式 | 默认模式（存储在 Redis） | JWT 模式 (`StpLogicJwtForSimple`) |
| 权限体系 | 有角色/权限控制 | 仅登录认证 |
| 路径范围 | `/mall-admin/**` | `/mall-portal/**` |
| 登录接口 | `/mall-admin/admin/login` | `/mall-portal/sso/login` |

### 6.2 SaReactorFilter 鉴权流程

`SaTokenConfig.java` 中的 `getSaReactorFilter()` 是网关鉴权的核心：

```java
@Bean
public SaReactorFilter getSaReactorFilter(IgnoreUrlsConfig ignoreUrlsConfig) {
    return new SaReactorFilter()
            .addInclude("/**")                          // 拦截所有路径
            .setExcludeList(ignoreUrlsConfig.getUrls()) // 排除白名单
            .setAuth(obj -> {
                // 第一步：OPTIONS 预检请求直接放行
                SaRouter.match(SaHttpMethod.OPTIONS).stop();

                // 第二步：前台会员认证 → /mall-portal/**
                SaRouter.match("/mall-portal/**",
                    r -> StpMemberUtil.checkLogin()).stop();

                // 第三步：后台管理员认证 → /mall-admin/**
                SaRouter.match("/mall-admin/**",
                    r -> StpUtil.checkLogin());

                // 第四步：动态权限校验（后台接口）
                Map<Object, Object> pathResourceMap =
                    redisTemplate.opsForHash()
                        .entries(AuthConstant.PATH_RESOURCE_MAP);
                // ... 路径匹配 + checkPermissionOr 或逻辑鉴权
            })
            .setError(this::handleException);  // 统一异常处理
}
```

**鉴权决策树**：

```
请求进入
    │
    ├─ 在白名单中？ ────────── YES ──→ 直接放行
    │
    ├─ OPTIONS 预检请求？ ──── YES ──→ 直接放行
    │
    ├─ 路径匹配 /mall-portal/**？
    │      └─ YES → StpMemberUtil.checkLogin()
    │              ├─ 已登录 → 放行
    │              └─ 未登录 → 返回 401 (unauthorized)
    │
    ├─ 路径匹配 /mall-admin/**？
    │      └─ YES → StpUtil.checkLogin()
    │              ├─ 已登录 → 继续权限校验
    │              │    ├─ 从 Redis 读取 PATH_RESOURCE_MAP
    │              │    ├─ AntPathMatcher 匹配当前路径所需权限
    │              │    ├─ checkPermissionOr(权限列表)
    │              │    │    ├─ 拥有任一权限 → 放行
    │              │    │    └─ 无权限 → 返回 403 (forbidden)
    │              └─ 未登录 → 返回 401 (unauthorized)
    │
    └─ 其他路径（如 /mall-auth/**、/mall-search/**）→ 无需认证，直接转发
```

### 6.3 白名单策略

```yaml
secure:
  ignore:
    urls:
      # === API 文档 ===
      - "/doc.html"
      - "/v3/api-docs/swagger-config"
      - "/*/v3/api-docs/default"
      - "/*/v3/api-docs"
      - "/*/swagger-ui/**"
      - "/webjars/**"

      # === 监控端点 ===
      - "/actuator/**"

      # === 认证服务（全部放行）===
      - "/mall-auth/**"

      # === 搜索服务（全部放行）===
      - "/mall-search/**"

      # === 前台商城公开接口 ===
      - "/mall-portal/sso/login"           # 登录
      - "/mall-portal/sso/register"        # 注册
      - "/mall-portal/sso/getAuthCode"     # 获取验证码
      - "/mall-portal/home/**"             # 首页
      - "/mall-portal/product/**"          # 商品浏览
      - "/mall-portal/brand/**"            # 品牌
      - "/mall-portal/alipay/**"           # 支付宝回调

      # === 后台管理公开接口 ===
      - "/mall-admin/admin/login"          # 管理后台登录
      - "/mall-admin/admin/register"       # 管理后台注册
      - "/mall-admin/minio/upload"         # 文件上传
```

### 6.4 动态权限校验机制

后台管理系统的权限校验不是硬编码的，而是 **从 Redis 动态读取**：

```java
// 从 Redis Hash 中获取所有接口→权限的映射
Map<Object, Object> pathResourceMap =
    redisTemplate.opsForHash()
        .entries(AuthConstant.PATH_RESOURCE_MAP);

// 使用 AntPathMatcher 匹配当前请求路径
String requestPath = SaHolder.getRequest().getRequestPath();
PathMatcher pathMatcher = new AntPathMatcher();

// 收集当前路径所需的所有权限
List<String> needPermissionList = new ArrayList<>();
for (Map.Entry<Object, Object> entry : pathResourceMap.entrySet()) {
    String pattern = (String) entry.getKey();
    if (pathMatcher.match(pattern, requestPath)) {
        needPermissionList.add((String) entry.getValue());
    }
}

// 拥有任意一个权限即可访问（或逻辑）
if (CollUtil.isNotEmpty(needPermissionList)) {
    StpUtil.checkPermissionOr(Convert.toStrArray(needPermissionList));
}
```

这意味着：添加新接口时，只需在 Redis 中配置路径与权限的映射关系，**无需重启网关**。

### 6.5 统一异常处理

```java
private CommonResult handleException(Throwable e) {
    // 设置 JSON 响应格式
    SaHolder.getResponse()
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setHeader("Access-Control-Allow-Origin", "*")
            .setHeader("Cache-Control", "no-cache");

    if (e instanceof NotLoginException) {
        return CommonResult.unauthorized(null);   // HTTP 401
    } else if (e instanceof NotPermissionException) {
        return CommonResult.forbidden(null);       // HTTP 403
    } else {
        return CommonResult.failed(e.getMessage()); // 通用错误
    }
}
```

---

## 七、跨域处理

```java
@Configuration
public class GlobalCorsConfig {
    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedMethod("*");           // 允许所有 HTTP 方法
        config.addAllowedOriginPattern("*");    // 允许所有来源
        config.addAllowedHeader("*");           // 允许所有请求头
        config.setAllowCredentials(true);       // 允许携带凭证（Cookie）
        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);       // ★ 注意：使用响应式 CorsWebFilter
    }
}
```

**关键点**：使用 `CorsWebFilter`（响应式）而非 `CorsFilter`（Servlet），因为 Spring Cloud Gateway 基于 WebFlux。

---

## 八、配置文件分层

```
application.yml (主配置)
    │
    ├─ application-dev.yml (开发环境)
    │       └─ Nacos: nacos:mall-gateway-dev.yaml (远程配置热更新)
    │
    └─ application-prod.yml (生产环境)
            └─ Nacos: nacos:mall-gateway-prod.yaml (远程配置热更新)
```

---

## 九、部署架构

该项目支持三种部署方式：

| 部署方式 | 说明 |
|----------|------|
| **本地开发** | `java -jar mall-gateway.jar --spring.profiles.active=dev` |
| **Docker Compose** | `mall-gateway` 容器，端口映射 8201:8201，连接 redis 和 nacos-registry 容器 |
| **Kubernetes** | Deployment（1 副本）+ Service（NodePort 30201→8201） |

---

## 十、核心设计亮点总结

1. **响应式网关**：基于 WebFlux + Netty，非阻塞 I/O，高并发场景下性能优于 Zuul 1.x
2. **双账号体系**：管理员（Sa-Token 默认模式）和会员（JWT 模式）独立认证，互不干扰
3. **动态权限**：接口权限从 Redis 读取，新增/修改权限无需重启网关
4. **白名单机制**：公开接口（登录、文档、监控等）免认证，配置集中管理
5. **路径重写**：`StripPrefix=1` 自动去除网关前缀，下游服务无需感知网关路径
6. **配置热更新**：通过 Nacos 配置中心实现远程配置的实时刷新
7. **API 文档聚合**：Knife4j 自动发现各微服务文档，在网关统一展示
8. **统一异常响应**：认证失败/权限不足统一返回 JSON 格式，前端友好

---

---

# 第二部分：Java 面试网关模块高频问题

## 面试题 1：Spring Cloud Gateway 和 Zuul 有什么区别？

**这是网关面试的必问题。**

| 维度 | Spring Cloud Gateway | Zuul 1.x | Zuul 2.x |
|------|---------------------|----------|----------|
| 底层实现 | WebFlux + Netty（响应式） | Servlet 2.5 + Tomcat（阻塞） | Netty（响应式） |
| IO 模型 | 非阻塞 NIO | 阻塞 BIO | 非阻塞 NIO |
| 线程模型 | 少量线程处理大量请求 | 一个请求一个线程 | 事件驱动 |
| 性能 | 高（适合高并发） | 较低 | 高 |
| 长连接支持 | 原生支持（WebSocket） | 需额外处理 | 原生支持 |
| Spring 生态 | 官方出品，深度集成 | Netflix 出品，Spring Cloud 集成 | Netflix 出品 |
| 维护状态 | 活跃维护 | 已停止维护 | 活跃维护（Netflix 内部） |
| API 路由方式 | Java 代码 / YAML 配置 | 过滤器 + 配置 | 过滤器 + 配置 |
| Spring Boot 版本 | 2.x / 3.x | 1.x | 2.x |

**面试回答建议**：
> Spring Cloud Gateway 是 Spring 官方推出的第二代网关，基于 WebFlux 响应式编程模型，底层使用 Netty 作为服务器，性能远优于基于 Servlet 的 Zuul 1.x。核心区别在于 IO 模型：Gateway 是非阻塞异步的，Zuul 1.x 是阻塞同步的。Zuul 1.x 已停止维护，现在主流选择都是 Gateway。

---

## 面试题 2：Spring Cloud Gateway 的核心工作流程是什么？

**面试官想考察你对 Gateway 内部机制的理解。**

Gateway 基于三大核心组件：**Route（路由）、Predicate（断言）、Filter（过滤器）**。

```
客户端请求
    │
    ▼
Gateway Handler Mapping  ──→  匹配 Route（路由）
    │                              │
    │                    通过 Predicate（断言）判断
    │                              │
    │                         匹配成功
    │                              ▼
    │                     Gateway Web Handler
    │                              │
    │                     执行 Filter Chain（过滤器链）
    │                              │
    │              ┌───────────────┼───────────────┐
    │              ▼               ▼               ▼
    │          Pre Filter      Pre Filter      Pre Filter
    │          (请求增强)      (鉴权)          (限流)
    │              │               │               │
    │              └───────────────┼───────────────┘
    │                              ▼
    │                     转发到目标微服务
    │                              │
    │                              ▼
    │              ┌───────────────┼───────────────┐
    │              ▼               ▼               ▼
    │         Post Filter     Post Filter     Post Filter
    │         (响应修改)      (日志记录)      (脱敏)
    │              │               │               │
    │              └───────────────┼───────────────┘
    │                              ▼
    ▼                         返回客户端
```

**源码核心类**：
- `RouteDefinitionRouteLocator`：加载路由定义
- `RoutePredicateHandlerMapping`：根据请求匹配路由
- `FilteringWebHandler`：执行过滤器链
- `NettyRoutingFilter`：使用 Netty 转发请求

**面试回答建议**：
> Gateway 的工作流程分为四步：第一，`RoutePredicateHandlerMapping` 接收请求，遍历所有路由定义的 Predicate 找到第一个匹配的 Route；第二，匹配成功后交给 `FilteringWebHandler`，它把该路由的所有 Filter 加上全局 Filter 组成过滤器链；第三，按顺序执行 Pre 类型的过滤器；第四，通过 `NettyRoutingFilter` 向目标服务发起请求，收到响应后再逆序执行 Post 过滤器，最后返回给客户端。

---

## 面试题 3：Gateway 的 Predicate（断言）有哪些类型？如何自定义？

**常见内置 Predicate**：

| Predicate | 说明 | 示例 |
|-----------|------|------|
| `Path` | 路径匹配 | `Path=/api/**` |
| `Method` | 请求方法 | `Method=GET,POST` |
| `Host` | 主机名匹配 | `Host=**.example.com` |
| `Header` | 请求头匹配 | `Header=X-Request-Id,\d+` |
| `Query` | 查询参数 | `Query=foo,bar` |
| `Cookie` | Cookie 匹配 | `Cookie=sessionId,abc` |
| `After/Before/Between` | 时间条件 | `After=2024-01-01T00:00:00+08:00` |
| `RemoteAddr` | IP 地址范围 | `RemoteAddr=192.168.0.0/16` |
| `Weight` | 权重路由（灰度发布） | `Weight=group1,80` |
| `XForwardedRemoteAddr` | 代理后的真实 IP | 配合 Nginx 使用 |

**自定义 Predicate**：

```java
// 1. 定义一个配置类
@Data
public class AuthPredicateConfig {
    private String tokenName;
}

// 2. 实现 AbstractRoutePredicateFactory
@Component
public class AuthRoutePredicateFactory
        extends AbstractRoutePredicateFactory<AuthPredicateConfig> {

    public AuthRoutePredicateFactory() {
        super(AuthPredicateConfig.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(AuthPredicateConfig config) {
        return exchange -> {
            // 自定义判断逻辑：检查请求头是否包含特定 Token
            String token = exchange.getRequest()
                .getHeaders().getFirst(config.getTokenName());
            return token != null && !token.isEmpty();
        };
    }
}

// 3. 配置使用
// spring.cloud.gateway.routes[0].predicates[0]=Auth=Authorization
```

**命名约定**：类名必须是 `配置名 + RoutePredicateFactory`，配置时使用 `配置名=参数`。

**面试回答建议**：
> Gateway 内置了 12 种 Predicate 工厂（After、Before、Between、Cookie、Header、Host、Method、Path、Query、RemoteAddr、Weight、XForwardedRemoteAddr）。自定义 Predicate 只需继承 `AbstractRoutePredicateFactory`，实现 `apply()` 方法返回一个 `Predicate<ServerWebExchange>`。类名必须以 `RoutePredicateFactory` 结尾，这样才能被自动识别。

---

## 面试题 4：Gateway 的 Filter 有哪些类型？它们的执行顺序是怎样的？

**Filter 分类**：

| 分类 | 作用域 | 说明 |
|------|--------|------|
| **GatewayFilter** | 单个路由 | 只对特定路由生效，通过 `filters` 配置 |
| **GlobalFilter** | 全局 | 对所有路由生效，通过 `@Component` 注册 |

**内置 GatewayFilter 示例**：

| Filter | 说明 |
|--------|------|
| `StripPrefix` | 去掉路径前缀（如本项目的 `StripPrefix=1`） |
| `AddRequestHeader` | 添加请求头 |
| `AddRequestParameter` | 添加请求参数 |
| `AddResponseHeader` | 添加响应头 |
| `PrefixPath` | 添加路径前缀 |
| `RedirectTo` | 重定向 |
| `RequestRateLimiter` | 限流 |
| `CircuitBreaker` | 熔断（集成 Resilience4j） |
| `Retry` | 重试 |
| `SetStatus` | 设置响应状态码 |
| `RewritePath` | 路径重写 |
| `SaveSession` | 保存 WebSession |

**执行顺序规则**：

1. 先执行 `order` 值小的 Filter
2. 数字越小优先级越高（默认 `Ordered.LOWEST_PRECEDENCE`）
3. `GlobalFilter` 默认 order = 0，可通过实现 `Ordered` 接口或 `@Order` 注解调整
4. Pre 逻辑按 order 从小到大执行，Post 逻辑按 order 从大到小执行

```
请求 → [Filter1(Pre)] → [Filter2(Pre)] → [Filter3(Pre)] → 下游服务
                                                              │
响应 ← [Filter1(Post)] ← [Filter2(Post)] ← [Filter3(Post)] ←
```

**面试回答建议**：
> Filter 分为 GatewayFilter（路由级别）和 GlobalFilter（全局）。执行顺序由 `@Order` 注解或 `Ordered` 接口控制，数字越小越先执行。请求阶段按 order 升序执行所有 Pre 逻辑，到达目标服务后响应阶段按 order 降序执行所有 Post 逻辑，形成一个完整的责任链。

---

## 面试题 5：如何在 Gateway 中实现自定义全局过滤器？

```java
@Component
@Order(-1)  // 数字越小越优先
public class RequestLogGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log =
        LoggerFactory.getLogger(RequestLogGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Pre 逻辑：请求前记录日志
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        log.info("请求进入网关: {} {}", method, path);

        long startTime = System.currentTimeMillis();

        // 继续执行过滤器链，并在响应返回后执行 Post 逻辑
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Post 逻辑：响应后记录耗时
            long duration = System.currentTimeMillis() - startTime;
            HttpStatus statusCode =
                exchange.getResponse().getStatusCode();
            log.info("请求完成: {} {} | {} | {}ms",
                method, path, statusCode, duration);
        }));
    }

    @Override
    public int getOrder() {
        return -1;  // 高优先级
    }
}
```

**面试回答建议**：
> 实现 `GlobalFilter` 和 `Ordered` 接口，添加 `@Component` 注册到 Spring 容器。核心方法是 `filter(ServerWebExchange, GatewayFilterChain)`，返回 `Mono<Void>`。想要在请求前做处理就写在 `chain.filter()` 之前；想在响应后做处理就用 `.then()` 追加一个后置逻辑。关键是理解 Gateway 是响应式的，所有操作都要返回 `Mono`。

---

## 面试题 6：Gateway 如何实现限流？

**方案一：内置 RequestRateLimiter + Redis**

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rate-limit-route
          uri: lb://mall-admin
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter:
                  replenishRate: 10     # 每秒令牌填充速率
                  burstCapacity: 20     # 令牌桶容量
                  requestedTokens: 1    # 每次请求消耗令牌数
                key-resolver: "#{@ipKeyResolver}"  # 限流 key
          predicates:
            - Path=/mall-admin/**
```

```java
// 基于 IP 的限流 Key 解析器
@Bean
public KeyResolver ipKeyResolver() {
    return exchange -> Mono.just(
        exchange.getRequest().getRemoteAddress()
            .getAddress().getHostAddress()
    );
}
```

Gateway 内置的限流基于 **令牌桶算法**（Token Bucket），使用 Redis + Lua 脚本实现分布式限流。

**方案二：集成 Sentinel**

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080  # Sentinel Dashboard
      filter:
        enabled: true
    gateway:
      routes:
        - id: mall-admin
          uri: lb://mall-admin
          predicates:
            - Path=/mall-admin/**
          filters:
            - Sentinel  # 启用 Sentinel 保护
```

限流策略可以在 Sentinel Dashboard 上动态配置。

**面试回答建议**：
> Gateway 限流主要有两种方案。一是内置的 `RequestRateLimiter`，基于 Redis + Lua 脚本实现令牌桶算法，需要配置速率参数和自定义 `KeyResolver`（按 IP、用户或接口限流）。二是集成 Sentinel，通过 Dashboard 动态配置限流和熔断规则，更灵活强大。令牌桶算法允许一定的突发流量，而漏桶算法严格平滑流量，Gateway 内置的是令牌桶。

---

## 面试题 7：Gateway 如何实现负载均衡？

Gateway 通过 `lb://` 前缀启用负载均衡，底层依赖 **Spring Cloud LoadBalancer**（替代了已废弃的 Ribbon）。

```yaml
# 配置示例（本项目）
uri: lb://mall-admin   # lb:// 触发负载均衡
```

**工作原理**：
1. 请求到达 Gateway
2. 识别 URI 前缀 `lb://`
3. 将服务名 `mall-admin` 交给 `ReactiveLoadBalancer`
4. 从 Nacos 获取该服务的所有可用实例列表
5. 根据负载均衡策略选择一个实例
6. 转发请求到选中的实例

**负载均衡策略自定义**：

```java
// 默认：轮询（Round Robin）
@Bean
public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
        Environment env, LoadBalancerClientFactory factory) {
    String name = env.getProperty("loadbalancer.client.name");
    return new RandomLoadBalancer(factory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
}
```

**面试回答建议**：
> Gateway 使用 `lb://服务名` 实现负载均衡，底层是 Spring Cloud LoadBalancer（Spring Cloud 2020.0 之后取代了 Ribbon）。它通过服务发现（Nacos/Eureka）获取实例列表，默认使用轮询策略。负载均衡发生在网关转发请求之前，是服务端负载均衡。

---

## 面试题 8：Gateway 的跨域如何处理？

**三种方式**：

**方式一：Gateway 配置（推荐，本项目采用）**
```java
@Bean
public CorsWebFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOriginPattern("*");
    config.addAllowedMethod("*");
    config.addAllowedHeader("*");
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source =
        new UrlBasedCorsConfigurationSource(new PathPatternParser());
    source.registerCorsConfiguration("/**", config);
    return new CorsWebFilter(source);
}
```

**方式二：YAML 配置**
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
```

**方式三：Nginx 配置**
```nginx
add_header Access-Control-Allow-Origin *;
add_header Access-Control-Allow-Methods 'GET, POST, OPTIONS';
add_header Access-Control-Allow-Headers '*';
```

**注意事项**：
- Gateway 必须使用响应式的 `CorsWebFilter`，不能使用 Servlet 的 `CorsFilter`
- 如果下游微服务也配置了跨域，可能会重复添加 CORS 头导致冲突，建议在网关统一处理

**面试回答建议**：
> 跨域问题是前后端分离项目最常见的配置。在 Gateway 中推荐使用 `CorsWebFilter`（注意是响应式的），统一在网关层处理跨域，避免下游每个微服务重复配置。有三点需要注意：一是必须用 `CorsWebFilter` 而非 `CorsFilter`；二是如果要携带 Cookie 必须设置 `allowCredentials(true)` 且来源不能用通配符 `*`；三是 OPTIONS 预检请求在鉴权过滤器中要直接放行。

---

## 面试题 9：Gateway + Nacos 如何实现动态路由？

**方式一：Nacos 配置中心存储路由**

```yaml
# 在 Nacos 中创建路由配置
spring:
  cloud:
    gateway:
      routes:
        - id: mall-admin
          uri: lb://mall-admin
          predicates:
            - Path=/mall-admin/**
          filters:
            - StripPrefix=1
```

```java
// 监听 Nacos 配置变化，动态刷新路由
@Component
@RefreshScope  // 配合 Nacos 配置自动刷新
public class DynamicRouteConfig implements ApplicationEventPublisherAware {

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    public void addRoute(RouteDefinition definition) {
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
    }

    public void deleteRoute(String routeId) {
        routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
    }
}
```

**方式二：数据库持久化 + 事件驱动**

```java
@Component
public class DynamicRouteService {

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    // 从数据库加载路由
    public void refreshRoutes() {
        List<RouteDefinition> routes = loadFromDB();
        routes.forEach(route ->
            routeDefinitionWriter.save(Mono.just(route)).subscribe()
        );
    }

    // 发布路由刷新事件
    @Autowired
    private ApplicationEventPublisher publisher;
    public void notifyChange() {
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }
}
```

**面试回答建议**：
> 动态路由的核心是 `RouteDefinitionWriter`，它提供了 `save` 和 `delete` 方法操作内存中的路由表。结合 Nacos 配置中心的 `@RefreshScope` 可以实现配置变更自动刷新路由。更复杂的方案是把路由存到数据库，通过 API 接口增删改路由，再发布 `RefreshRoutesEvent` 事件通知 Gateway 重新加载。注意 `RouteDefinitionWriter` 是响应式的，返回的是 `Mono`，需要调用 `.subscribe()` 触发执行。

---

## 面试题 10：Gateway 高可用和容错怎么做？

**1. 多实例部署**
```
                Nginx (负载均衡)
               /        |        \
              v         v         v
        Gateway-1   Gateway-2   Gateway-3
              |         |         |
              └─────────┼─────────┘
                        v
                 Nacos 注册中心
```

网关本身是无状态的（认证状态在 Redis/JWT 中），所以可以直接水平扩展。

**2. 熔断（CircuitBreaker）**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: mall-admin
          uri: lb://mall-admin
          filters:
            - name: CircuitBreaker
              args:
                name: adminCircuitBreaker
                fallbackUri: forward:/fallback/admin
          predicates:
            - Path=/mall-admin/**
```

```java
@RestController
public class FallbackController {
    @RequestMapping("/fallback/admin")
    public Mono<CommonResult> adminFallback() {
        return Mono.just(CommonResult.failed("服务暂时不可用，请稍后重试"));
    }
}
```

**3. 重试（Retry）**
```yaml
filters:
  - name: Retry
    args:
      retries: 3                 # 重试次数
      statuses: BAD_GATEWAY      # 触发重试的状态码
      methods: GET               # 触发重试的方法
      backoff:
        firstBackoff: 100ms      # 首次重试间隔
        maxBackoff: 500ms        # 最大重试间隔
```

**面试回答建议**：
> 网关高可用分几个层面：一是多实例部署，前端用 Nginx 或云平台的负载均衡做流量分发，网关本身是无状态的很容易水平扩展。二是下游服务熔断，集成 Resilience4j CircuitBreaker，当下游服务不可用时快速失败返回兜底响应，避免雪崩。三是请求重试，对幂等请求（GET）可以设置重试次数和退避策略。四是网关本身设置超时时间，防止慢请求占用线程资源。

---

## 面试题 11：Gateway 的 StripPrefix 和 RewritePath 有什么区别？

本项目使用了 `StripPrefix=1`，这是一个高频考点。

**StripPrefix**：按 `/` 分割路径，去掉前面 N 段。

```
请求:  /mall-admin/admin/login
StripPrefix=1 后:  /admin/login
StripPrefix=2 后:  /login
```

**RewritePath**：正则表达式替换。

```yaml
filters:
  - RewritePath=/api/(?<segment>.*), /$\{segment}
```

```
请求:  /api/admin/login
RewritePath 后:  /admin/login
```

| 维度 | StripPrefix | RewritePath |
|------|------------|-------------|
| 配置方式 | 数字 | 正则表达式 |
| 灵活性 | 简单粗暴 | 灵活强大 |
| 性能 | 更高 | 正则匹配有开销 |
| 适用场景 | 统一前缀去除 | 复杂路径转换 |

---

## 面试题 12：为什么 Gateway 要排除 spring-boot-starter-web？

**核心答案**：Spring Cloud Gateway 基于 **Spring WebFlux**（响应式），WebFlux 默认运行在 **Netty** 之上。而 `spring-boot-starter-web` 引入了 Spring MVC，默认运行在 **Tomcat** 之上。

**两者冲突的根本原因**：
- WebFlux 使用 `org.springframework.web.reactive` 包
- WebMVC 使用 `org.springframework.web.servlet` 包
- 两者不能同时作为 Web 容器存在，Spring Boot 检测到两者共存时会以 WebMVC 优先，导致 Gateway 无法正常启动

**本项目处理方式**：
```xml
<dependency>
    <groupId>com.macro.mall</groupId>
    <artifactId>mall-common</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

---

## 面试题 13：JWT Token 和 Session Token 在网关层面有什么区别？

本项目恰好同时使用了两者：
- **管理员**：Session Token（Sa-Token 默认模式，存在 Redis）
- **会员**：JWT Token（`StpLogicJwtForSimple`）

| 维度 | JWT Token | Session Token |
|------|-----------|---------------|
| 无状态 | 是，服务端不存 | 否，需要 Redis 存储 |
| 吊销难度 | 困难（需黑名单） | 简单（删 Redis key） |
| 网关认证 | 直接解析 JWT 验证签名 | 查询 Redis 验证 |
| 性能 | 高（无网络 IO） | 需访问 Redis |
| 信息承载 | Token 自身含用户信息 | Token 只是凭证，信息在 Redis |
| 扩展性 | 天然支持分布式 | 需共享 Redis |

**面试回答建议**：
> JWT 是无状态的，Token 本身就包含了用户信息和签名，网关可以直接解析验证，不需要访问 Redis，性能更好，但也导致无法主动吊销（除非加黑名单）。Session Token 是传统模式，Token 只是一个随机字符串作为 key，真正的用户信息存在 Redis 里，可以随时删除实现强制下线。本项目前台会员用 JWT 是因为移动端对无状态要求更高，后台管理员用 Session Token 是因为需要更灵活的权限控制和强制下线能力。

---

## 面试题 14：如何保证 Gateway 中请求/响应的线程安全？

Spring Cloud Gateway 基于 WebFlux 和 Reactor，它的线程安全保证来自几个方面：

**1. 不可变对象**
`ServerWebExchange` 中的 `ServerHttpRequest` 和 `ServerHttpResponse` 是不可变的。修改请求时需要使用 `mutate()` 方法创建新的对象。

```java
// 正确做法：创建新的 Request
ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
    .header("X-Custom-Header", "value")
    .build();

// 创建新的 Exchange
ServerWebExchange mutatedExchange = exchange.mutate()
    .request(mutatedRequest)
    .build();

return chain.filter(mutatedExchange);  // 传递新的不可变对象
```

**2. Reactor 的线程模型**
```java
// Reactor 保证同一时间只有一个线程执行 Mono/Flux 的操作
Mono.just("data")
    .map(s -> s.toUpperCase())     // 线程 A
    .flatMap(s -> callRemote(s))   // 可能切换到线程 B
    .subscribe();
```

**3. 避免在 Filter 中使用 ThreadLocal**
由于响应式编程中线程会切换，ThreadLocal 的值可能在后续操作中丢失。需要使用 Reactor Context 替代：

```java
// ❌ 错误：ThreadLocal 在响应式中不可靠
// ThreadLocal<String> userId = new ThreadLocal<>();

// ✅ 正确：使用 Reactor Context
return chain.filter(exchange)
    .contextWrite(Context.of("userId", "12345"));
```

---

## 面试题 15：谈谈你在网关层遇到过什么坑，怎么解决的？

**这是一个开放题，结合本项目的设计来回答：**

**坑1：WebFlux 和 WebMVC 依赖冲突**
- 现象：启动报错，类找不到或两个 Web 容器冲突
- 解决：Gateway 模块排除 `spring-boot-starter-web`（本项目 pom.xml 里有体现）

**坑2：CORS 跨域配置不生效**
- 现象：前端请求仍然跨域报错
- 原因：用了 Servlet 的 `CorsFilter` 而不是响应式的 `CorsWebFilter`
- 解决：改用 `CorsWebFilter`，注意 OPTIONS 预检请求也要在鉴权过滤器中放行

**坑3：请求体只能读取一次**
- 现象：在 Gateway 中读取了 RequestBody 后，下游服务收不到请求体
- 解决：使用 `ModifyRequestBodyGatewayFilterFactory` 或手动缓存 Body

```java
// 使用缓存装饰器
ServerRequest cachedRequest = exchange.getRequest().mutate()
    .header("X-Body-Cached", "true")
    .build();
```

**坑4：Nacos 动态路由不生效**
- 现象：修改 Nacos 配置后路由没变化
- 原因：路由没有配置 `@RefreshScope` 或没有触发刷新事件
- 解决：配合 `@RefreshScope` + `RefreshRoutesEvent` 实现路由热更新

**坑5：长连接/WebSocket 代理失败**
- 现象：WebSocket 连接建立后立即断开
- 原因：Gateway 默认的路由配置不支持 WebSocket
- 解决：Gateway 对 WebSocket 有原生支持，配置路由即可，但要确保没有不兼容的 Filter 中断连接

---

## 附：面试速查表

| 问题 | 一句话答案 |
|------|-----------|
| Gateway vs Zuul | WebFlux 响应式 vs Servlet 阻塞，性能差异巨大 |
| 三大核心组件 | Route（路由）、Predicate（断言）、Filter（过滤器） |
| StripPrefix 作用 | 按 `/` 分割去掉路径前缀 |
| `lb://` 含义 | 启用负载均衡的服务发现调用 |
| 为什么排除 web | WebFlux 和 WebMVC 不兼容 |
| 跨域注意 | 使用 `CorsWebFilter` 而非 `CorsFilter` |
| 动态路由 | `RouteDefinitionWriter` + Nacos/@RefreshScope |
| 限流方案 | 内置 RequestRateLimiter（令牌桶）或集成 Sentinel |
| GlobalFilter vs GatewayFilter | 全局生效 vs 路由级别生效 |
| 过滤器执行顺序 | order 越小越优先，pre 升序 post 降序 |
| 负载均衡 | `lb://` + Spring Cloud LoadBalancer + Nacos 服务发现 |
| JWT vs Session | 无状态高扩展 vs 灵活管控可吊销 |
| 熔断降级 | Resilience4j CircuitBreaker + fallbackUri |

---

*学习日期：2026-07-07*
