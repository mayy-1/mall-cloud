# mall-cloud 公共模块学习笔记

---

# 第一部分：mall-common 模块完整梳理

## 一、模块定位

`mall-common` 是 mall-cloud 微服务电商平台的 **公共基础模块**，被所有其他微服务模块依赖。它提供统一的 API 响应封装、Redis 工具类、全局异常处理、AOP 日志切面、自定义校验注解、认证常量定义等基础设施能力，是整个项目的基石。

---

## 二、技术架构全景

```
                    所有微服务模块
                        │
                        v
            ┌───────────────────────────┐
            │       mall-common         │
            │      (公共基础模块)         │
            │                           │
            │  ┌─────────────────────┐  │
            │  │   api/              │  │  ← 统一响应封装
            │  │   CommonResult      │  │
            │  │   CommonPage        │  │
            │  │   ResultCode        │  │
            │  │   IErrorCode        │  │
            │  └─────────────────────┘  │
            │  ┌─────────────────────┐  │
            │  │   service/          │  │  ← Redis 操作封装
            │  │   RedisService      │  │
            │  │   RedisServiceImpl  │  │
            │  └─────────────────────┘  │
            │  ┌─────────────────────┐  │
            │  │   exception/        │  │  ← 统一异常处理
            │  │   ApiException      │  │
            │  │   GlobalException   │  │
            │  │   Handler           │  │
            │  │   Asserts           │  │
            │  └─────────────────────┘  │
            │  ┌─────────────────────┐  │
            │  │   log/              │  │  ← AOP 日志切面
            │  │   WebLogAspect      │  │
            │  └─────────────────────┘  │
            │  ┌─────────────────────┐  │
            │  │   config/           │  │  ← Redis 基础配置
            │  │   BaseRedisConfig   │  │
            │  └─────────────────────┘  │
            │  ┌─────────────────────┐  │
            │  │   validator/        │  │  ← 自定义校验
            │  │   FlagValidator     │  │
            │  └─────────────────────┘  │
            └───────────────────────────┘
```

---

## 三、目录结构

```
mall-common/
├── pom.xml
└── src/main/
    ├── java/com/mym/mall/
    │   ├── common/
    │   │   ├── annotation/
    │   │   │   └── CacheException.java        # 缓存异常注解
    │   │   ├── api/
    │   │   │   ├── CommonPage.java            # 分页数据封装 ★
    │   │   │   ├── CommonResult.java           # 统一返回对象 ★★
    │   │   │   ├── IErrorCode.java            # 错误码接口
    │   │   │   └── ResultCode.java            # 操作码枚举
    │   │   ├── config/
    │   │   │   └── BaseRedisConfig.java        # Redis 基础配置 ★
    │   │   ├── constant/
    │   │   │   └── AuthConstant.java           # 权限常量 ★
    │   │   ├── domain/
    │   │   │   └── WebLog.java                 # 日志模型
    │   │   ├── dto/
    │   │   │   └── UserDto.java                # 用户信息 DTO
    │   │   ├── exception/
    │   │   │   ├── ApiException.java           # 自定义异常 ★
    │   │   │   ├── Asserts.java                # 断言工具
    │   │   │   └── GlobalExceptionHandler.java # 全局异常处理 ★
    │   │   ├── log/
    │   │   │   └── WebLogAspect.java           # AOP 日志切面 ★
    │   │   └── service/
    │   │       ├── RedisService.java           # Redis 操作接口
    │   │       └── impl/
    │   │           └── RedisServiceImpl.java   # Redis 操作实现
    │   └── validator/
    │       ├── FlagValidator.java              # 状态校验注解
    │       └── FlagValidatorClass.java         # 校验器实现
    └── resources/
        └── logback-spring.xml                  # 日志框架配置 ★
```

---

## 四、核心依赖解读（pom.xml）

| 依赖 | 作用 |
|------|------|
| `pagehelper` | MyBatis 分页插件，`CommonPage` 依赖它转换分页结果 |
| `spring-boot-starter-web` | Spring Web MVC 支持，提供 HTTP 服务基础 |
| `spring-boot-starter-data-redis` | Redis 操作，`RedisService` 和 `BaseRedisConfig` 的核心依赖 |
| `spring-data-commons` | Spring Data 通用分页抽象，`CommonPage` 的另一个适配来源 |
| `knife4j-openapi3-jakarta-spring-boot-starter` | Knife4j API 文档 (OpenAPI 3) |
| `springdoc-openapi-starter-webmvc-ui` | SpringDoc OpenAPI UI |
| `logstash-logback-encoder` | Logstash JSON 编码器，用于 AOP 日志输出到 Logstash |
| `janino` | Logback 条件表达式引擎，用于 logback-spring.xml 中 `<if>` 标签 |
| `spring-boot-starter-validation` | Jakarta Bean Validation，`FlagValidator` 依赖 |
| `spring-cloud-starter-loadbalancer` | Spring Cloud 负载均衡（Feign 调用需要） |

**关键设计点**：mall-common 不包含任何业务逻辑，只提供通用的技术组件。它不直接依赖任何业务服务，保证了模块的纯净性。所有被依赖的第三方库都是通过 pom.xml 显式声明而非父 POM 继承，这使得公共模块的依赖范围可控。

---

## 五、核心组件详解

### 5.1 统一返回对象 — CommonResult

```java
public class CommonResult<T> {
    private long code;
    private String message;
    private T data;

    // 成功
    public static <T> CommonResult<T> success(T data) { ... }
    public static <T> CommonResult<T> success(T data, String message) { ... }

    // 失败
    public static <T> CommonResult<T> failed() { ... }
    public static <T> CommonResult<T> failed(String message) { ... }
    public static <T> CommonResult<T> failed(IErrorCode errorCode) { ... }

    // 参数验证失败
    public static <T> CommonResult<T> validateFailed() { ... }
    public static <T> CommonResult<T> validateFailed(String message) { ... }

    // 未登录/未授权
    public static <T> CommonResult<T> unauthorized(T data) { ... }
    public static <T> CommonResult<T> forbidden(T data) { ... }
}
```

**设计模式**：静态工厂方法。所有 API 返回都使用同样的 JSON 结构：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 5.2 操作码枚举 — ResultCode

| 枚举值 | code | 说明 |
|--------|------|------|
| `SUCCESS` | 200 | 操作成功 |
| `FAILED` | 500 | 操作失败 |
| `VALIDATE_FAILED` | 404 | 参数检验失败 |
| `UNAUTHORIZED` | 401 | 暂未登录或 token 已过期 |
| `FORBIDDEN` | 403 | 没有相关权限 |

**扩展能力**：通过实现 `IErrorCode` 接口，业务模块可以自定义错误码枚举。

### 5.3 分页数据封装 — CommonPage

```java
public class CommonPage<T> {
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPage;
    private Long total;
    private List<T> list;

    // 适配 PageHelper 的分页结果
    public static <T> CommonPage<T> restPage(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        // ...
    }

    // 适配 Spring Data 的分页结果
    public static <T> CommonPage<T> restPage(Page<T> pageInfo) {
        // ...
    }
}
```

**双适配设计**：同时支持 PageHelper（MyBatis）和 Spring Data Page 两种分页方式。

### 5.4 Redis 操作封装 — RedisService

```java
public interface RedisService {
    // === String 操作 ===
    void set(String key, Object value, long time);
    Object get(String key);
    Boolean del(String key);
    Long incr(String key, long delta);
    Long decr(String key, long delta);

    // === Hash 操作 ===
    Object hGet(String key, String hashKey);
    Boolean hSet(String key, String hashKey, Object value, long time);
    Map<Object, Object> hGetAll(String key);
    void hDel(String key, Object... hashKey);

    // === Set 操作 ===
    Set<Object> sMembers(String key);
    Long sAdd(String key, Object... values);
    Boolean sIsMember(String key, Object value);

    // === List 操作 ===
    List<Object> lRange(String key, long start, long end);
    Long lPush(String key, Object value);
    Long lRemove(String key, long count, Object value);
}
```

**封装价值**：
- 统一了时间单位（全部使用秒，内部转 `TimeUnit.SECONDS`）
- 简化了 API 调用（不需要显式操作 `RedisTemplate`）
- 提供了带过期时间的便捷方法

### 5.5 Redis 序列化配置 — BaseRedisConfig

```java
public class BaseRedisConfig {
    @Bean
    public RedisSerializer<Object> redisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // Key 用 StringRedisSerializer
        // Value 用 Jackson2JsonRedisSerializer
    }
}
```

**关键配置**：
- **Key 序列化**：`StringRedisSerializer`（Redis 中 key 可读）
- **Value 序列化**：`Jackson2JsonRedisSerializer`，开启了 `activateDefaultTyping` — **必须设置此项**，否则 JSON 反序列化时不知道目标类型，会变成 `LinkedHashMap`
- **缓存 TTL**：`RedisCacheManager` 默认缓存有效期 1 天

### 5.6 全局异常处理 — GlobalExceptionHandler

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ApiException.class)
    public CommonResult handle(ApiException e) { ... }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public CommonResult handleValidException(MethodArgumentNotValidException e) { ... }

    @ExceptionHandler(value = BindException.class)
    public CommonResult handleValidException(BindException e) { ... }
}
```

**三级异常处理**：
1. `ApiException` → 返回 `failed`，可携带自定义错误码
2. `MethodArgumentNotValidException` → 校验 `@RequestBody` 参数时抛出，返回 `validateFailed`
3. `BindException` → 校验表单参数时抛出，返回 `validateFailed`

使用 `@ControllerAdvice` 对整个 Spring 容器的 Controller 生效。

### 5.7 AOP 日志切面 — WebLogAspect

```java
@Aspect
@Component
@Order(1)
public class WebLogAspect {
    @Around("execution(public * com.mym.mall.controller.*.*(..))"
          + "||execution(public * com.mym.mall.*.controller.*.*(..))")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        // 封装 WebLog 对象
        WebLog webLog = new WebLog();
        webLog.setSpendTime((int)(endTime - startTime));
        // ...

        // 通过 Logstash Marker 输出结构化 JSON 日志
        LOGGER.info(Markers.appendEntries(logMap), JSONUtil.parse(webLog).toString());
        return result;
    }
}
```

**切入点表达式**：匹配两条路径模式
- `com.mym.mall.controller.*.*(..)` — 一级控制器
- `com.mym.mall.*.controller.*.*(..)` — 二级控制器（如 mall.auth.controller）

**日志内容**：记录 URL、HTTP 方法、请求参数、返回结果、耗时、接口描述（从 `@Operation` 注解提取）

### 5.8 Logback 日志配置 — logback-spring.xml

多层次的日志输出体系：

```
┌─────────────────────────────────────────────┐
│                   root (DEBUG)               │
│                                               │
│   ┌─────────────┐  ┌──────────────┐          │
│   │  CONSOLE    │  │  FILE_DEBUG   │          │
│   │ (控制台输出)  │  │ (DEBUG文件)   │          │
│   └─────────────┘  └──────────────┘          │
│                                               │
│   ┌──────────────┐                            │
│   │  FILE_ERROR  │                            │
│   │ (ERROR文件)   │                            │
│   └──────────────┘                            │
│                                               │
│   条件性启用 (logstash.enableInnerLog=true)    │
│   ┌──────────────────────────────────────┐   │
│   │  LOG_STASH_DEBUG    (端口 4560)      │   │
│   │  LOG_STASH_ERROR    (端口 4561)      │   │
│   │  LOG_STASH_BUSINESS (端口 4562)      │   │
│   │  LOG_STASH_RECORD   (端口 4563)      │   │
│   └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

**文件滚动策略**：
- 按大小和日期滚动（`SizeAndTimeBasedRollingPolicy`）
- 单文件最大 10MB，保留 30 天
- DEBUG 和 ERROR 分目录存放

### 5.9 自定义校验注解 — FlagValidator

```java
@Constraint(validatedBy = FlagValidatorClass.class)
public @interface FlagValidator {
    String[] value() default {};
    String message() default "flag is not found";
}
```

**用法示例**：
```java
@FlagValidator({"0", "1"})
private Integer showStatus;
```

校验 `showStatus` 的值必须是 0 或 1，null 值视为合法（可配合 `@NotNull` 使用）。

### 5.10 权限常量 — AuthConstant

```java
public interface AuthConstant {
    String AUTHORITY_PREFIX = "ROLE_";
    String ADMIN_CLIENT_ID = "admin-app";     // 后台管理
    String PORTAL_CLIENT_ID = "portal-app";   // 前台商城
    String PATH_RESOURCE_MAP = "auth:pathResourceMap";  // 动态权限 Redis Key
    String JWT_TOKEN_HEADER = "Authorization";
    String JWT_TOKEN_PREFIX = "Bearer ";
    String STP_MEMBER_INFO = "memberInfo";    // Sa-Token 会员信息
    String STP_ADMIN_INFO = "adminInfo";      // Sa-Token 管理员信息
}
```

**设计价值**：将散落在各模块的魔法字符串集中到常量接口中管理，修改时只需改一处。

---

## 六、核心设计亮点总结

1. **统一响应封装**：`CommonResult` + `CommonPage` 提供了全项目统一的 API 返回格式
2. **静态工厂方法**：`CommonResult.success()/.failed()/.validateFailed()/.unauthorized()/.forbidden()` 语义化 API 构建
3. **多级异常处理**：`@ControllerAdvice` 全局捕获，业务异常 / 校验异常分别处理
4. **AOP 无侵入日志**：通过切面自动记录 Controller 每次请求的耗时和参数，无需在业务代码中手动打日志
5. **Redis 工具封装**：四种数据结构（String/Hash/Set/List）的完整操作，统一时间单位
6. **日志多通道输出**：控制台 + 文件 + 条件性 Logstash，满足开发调试和生产监控双重需求
7. **JSON 序列化类型保留**：`activateDefaultTyping` 确保反序列化时类型不丢失
8. **自定义校验扩展**：`FlagValidator` 演示了 Jakarta Validation 的自定义校验注解模式

---

---

# 第二部分：Java 面试公共模块高频问题

## 面试题 1：CommonResult 为什么使用静态工厂方法而不是直接 new？

**静态工厂方法的优势**：
1. **语义化**：`CommonResult.success(data)` 比 `new CommonResult(200, "操作成功", data)` 更易读
2. **代码复用**：每次成功不需要重复传 code=200 和 message
3. **集中管理**：所有状态码在 `ResultCode` 枚举中统一管理
4. **类型推断**：泛型方法可以自动推断 `T` 的类型

**面试回答建议**：
> 静态工厂方法相比构造器有三个优势：第一，方法名直接表达了语义（success/failed/unauthorized），比 new 构造器更可读；第二，集中管理了状态码和默认消息，避免魔法数字分散在各处；第三，方法签名可以复用，成功调用的代码每次都很简洁。

---

## 面试题 2：GlobalExceptionHandler 的工作原理是什么？

`@ControllerAdvice` + `@ExceptionHandler` 组合是 Spring MVC 提供的全局异常处理机制：

```
请求 → DispatcherServlet → HandlerAdapter → Controller
                                         │
                                    ┌────┴─────┐
                                    │ 抛异常？   │
                                    └────┬─────┘
                                    YES  │
                                         v
                                 @ExceptionHandler
                                    ├─ ApiException.class
                                    ├─ MethodArgumentNotValidException.class
                                    └─ BindException.class
                                         │
                                         v
                                返回 CommonResult (JSON)
```

**面试回答建议**：
> `@ControllerAdvice` 注解切入了所有 Controller 的执行流程，`@ExceptionHandler` 指定了要捕获的异常类型。当 Controller 抛出异常时，Spring 会遍历所有的 `ExceptionHandler`，找到第一个能处理该异常的方法。项目中定义了三级处理：ApiException 返回业务错误，MethodArgumentNotValidException 和 BindException 返回参数校验错误。

---

## 面试题 3：AOP 日志切面 @Around 和 @Before 的区别？

| 维度 | @Around | @Before | @AfterReturning |
|------|---------|---------|----------------|
| 控制方法执行 | 可以（需手动调用 `joinPoint.proceed()`） | 不能 | 不能 |
| 获取返回值 | 可以 | 不能 | 可以（通过 returning） |
| 计算耗时 | 可以（前后记录时间） | 不能（不知道结束时间） | 不能（不知道开始时间） |
| 修改返回值 | 可以 | 不能 | 不能 |
| 异常处理 | 需要 try-catch | 异常会中断 | 异常不会执行 |

本项目使用 `@Around` 正是因为需要**同时记录开始时间和结束时间来计算耗时**。

---

## 面试题 4：Redis 序列化中 activateDefaultTyping 的作用？

**问题表象**：不加 `activateDefaultTyping`，存入 Redis 的 `UserDto` 对象取出来会变成 `LinkedHashMap`，导致类型转换异常。

**根本原因**：Jackson 默认在序列化时不写入类型信息。反序列化时不知道目标类型，只能反序列化为 `Map`。

**配置效果**：
```java
objectMapper.activateDefaultTyping(
    LaissezFaireSubTypeValidator.instance,  // 白名单类型验证器
    ObjectMapper.DefaultTyping.NON_FINAL    // 非 final 类型都写入类型信息
);
```

JSON 中会多出 `@class` 字段：`{"@class":"com.mym.mall.common.dto.UserDto","username":"admin",...}`，反序列化时根据此字段确定目标类型。

**面试回答建议**：
> Jackson 默认不输出类型信息，反序列化 `Object` 类型的 value 时会变成 `LinkedHashMap`。`activateDefaultTyping` 会在 JSON 中添加 `@class` 字段记录完整类名，反序列化时就能找到正确的目标类型。`NON_FINAL` 表示除了 `final` 类（如 `String`）外都写入类型信息。

---

## 面试题 5：Logback 中用 janino 实现条件判断的原理？

janino 是一个轻量级的 Java 编译器，可以在运行时编译和执行 Java 代码。Logback 用它来实现 `<if>` 标签内的条件表达式。

```xml
<condition class="ch.qos.logback.core.boolex.PropertyEqualityCondition">
    <key>ENABLE_LOG_STASH</key>
    <value>true</value>
</condition>
<if>
    <then>
        <!-- 只有 ENABLE_LOG_STASH=true 时才启用 Logstash 输出 -->
    </then>
</if>
```

**面试回答建议**：
> Logback 本身不支持条件判断，janino 是一个嵌入式 Java 编译器，让 Logback 能在配置文件中执行条件表达式。项目用这个机制实现"开发环境不输出 Logstash，生产环境输出"的切换，完全由配置属性 `logstash.enableInnerLog` 控制，不需要维护两套配置文件。

---

## 面试题 6：Bean Validation 的自定义校验注解是如何工作的？

**三个组件**：
1. `@Constraint(validatedBy = FlagValidatorClass.class)` — 绑定校验器
2. `ConstraintValidator<FlagValidator, Integer>` — 实现校验逻辑
3. `initialize()` — 从注解读取参数，`isValid()` — 执行校验

**面试回答建议**：
> 自定义校验注解需要三步：一是定义注解，用 `@Constraint` 指定校验器类；二是实现 `ConstraintValidator<A extends Annotation, T>` 接口；三是在 `initialize` 中读取注解参数，在 `isValid` 中执行校验逻辑。`isValid` 返回 false 时框架抛出 `MethodArgumentNotValidException`。

---

## 面试速查表

| 问题 | 一句话答案 |
|------|-----------|
| CommonResult 设计 | 静态工厂方法，语义化 + 统一管理状态码 |
| @ControllerAdvice | AOP 拦截所有 Controller，匹配 @ExceptionHandler |
| AOP 日志 | @Around 记录前后时间差 → 计算耗时 |
| Redis 序列化 | activateDefaultTyping 防反序列化变 Map |
| logback 条件 | janino 编译器，运行时判断 ENABLE_LOG_STASH |
| 自定义校验 | @Constraint + ConstraintValidator 实现类 |
| CommonPage 双适配 | PageHelper 的 PageInfo + Spring Data 的 Page |

---

*学习日期：2026-07-08*
