# mall-cloud API 接口模块学习笔记

---

# 第一部分：mall-api 模块完整梳理

## 一、模块定位

`mall-api` 是 mall-cloud 微服务电商平台的 **Feign 接口与 DTO 集中管理模块**，采用"黑马商城风格"设计。它将所有跨服务调用的 Feign 接口定义和跨服务传输的 DTO 集中在一个纯接口模块中，各业务服务通过依赖 mall-api 获得调用其他服务的能力，避免了微服务之间的循环依赖问题。

---

## 二、技术架构全景

```
  product-service    cart-service    portal-service   ...
        │                 │                │
        │                 │                │
        └─────────┬───────┴────────┬───────┘
                  │                │
                  v                v
          ┌──────────────────────────────┐
          │          mall-api            │
          │   (Feign 接口 + DTO 集中)     │
          │                              │
          │  ┌────────────────────────┐  │
          │  │  client/               │  │
          │  │  BrandClient           │  │  → product-service /brand
          │  │  ProductClient  ★      │  │  → product-service /product
          │  │  CartClient            │  │  → cart-service /cart
          │  │  MemberClient          │  │  → member-service
          │  │  UserClient            │  │  → user-service /admin
          │  │  HomeClient            │  │  → marketing-service /home
          │  │  MarketingClient       │  │  → marketing-service /flash
          │  │  SubjectClient         │  │  → marketing-service /subject
          │  └────────────────────────┘  │
          │  ┌────────────────────────┐  │
          │  │  config/               │  │
          │  │  DefaultFeignConfig    │  │  ← 日志级别 + Token 透传
          │  └────────────────────────┘  │
          │  ┌────────────────────────┐  │
          │  │  dto/                  │  │
          │  │  BrandDTO              │  │
          │  │  ProductDTO     ★      │  │
          │  │  CartItemDTO           │  │
          │  │  MemberDTO             │  │
          │  │  SubjectDTO            │  │
          │  │  StockDeductDTO        │  │
          │  └────────────────────────┘  │
          └──────────────────────────────┘
```

---

## 三、目录结构

```
mall-api/
├── pom.xml
└── src/main/java/com/mall/api/
    ├── client/
    │   ├── BrandClient.java          # 品牌 Feign 接口
    │   ├── CartClient.java           # 购物车 Feign 接口
    │   ├── HomeClient.java           # 首页内容 Feign 接口
    │   ├── MarketingClient.java      # 营销/秒杀 Feign 接口
    │   ├── MemberClient.java         # 会员 Feign 接口
    │   ├── ProductClient.java        # 商品 Feign 接口（最核心）★
    │   ├── SubjectClient.java        # 专题 Feign 接口
    │   └── UserClient.java           # 管理员 Feign 接口
    ├── config/
    │   └── DefaultFeignConfig.java    # Feign 全局配置 ★
    └── dto/
        ├── BrandDTO.java              # 品牌传输对象
        ├── CartItemDTO.java           # 购物车项传输对象
        ├── MemberDTO.java             # 会员传输对象
        ├── ProductDTO.java            # 商品传输对象
        ├── StockDeductDTO.java        # 库存扣减请求对象
        └── SubjectDTO.java            # 专题传输对象
```

---

## 四、核心依赖解读（pom.xml）

| 依赖 | 作用 |
|------|------|
| `mall-common` | 公共模块，提供 `CommonResult`、`CommonPage` 等统一响应封装 |
| `spring-cloud-starter-openfeign` | OpenFeign 声明式 HTTP 客户端，核心依赖 |

**关键设计点**：mall-api 只依赖 mall-common 和 OpenFeign，**不依赖任何具体业务模块**（不依赖 user-service、product-service 等）。这保证了它能作为独立的 API 层被所有模块安全引用。

---

## 五、Feign 客户端详解

### 5.1 服务-Client 映射总览

| Feign Client | @FeignClient(name) | 基础路径 | 方法数 |
|-------------|-------------------|----------|--------|
| `BrandClient` | `product-service` | `/brand` | 2 |
| `ProductClient` ★ | `product-service` | `/product` | 12 |
| `CartClient` | `cart-service` | `/cart` | 2 |
| `MemberClient` | `member-service` | (无) | 2 |
| `UserClient` | `user-service` | `/admin` | 3 |
| `HomeClient` | `marketing-service` | `/home` | 2 |
| `MarketingClient` | `marketing-service` | `/flash` | 2 |
| `SubjectClient` | `marketing-service` | `/subject` | 2 |

**服务依赖关系可视化**：
```
product-service ←── BrandClient, ProductClient
cart-service    ←── CartClient
member-service  ←── MemberClient
user-service    ←── UserClient
marketing-service ←── HomeClient, MarketingClient, SubjectClient
```

### 5.2 ProductClient — 最核心的 Feign 接口

```java
@FeignClient(name = "product-service", path = "/product")
public interface ProductClient {

    // 按 ID 查询单个商品
    @GetMapping("/{id}")
    CommonResult<ProductDTO> getById(@PathVariable Long id);

    // 查询商品详情
    @GetMapping("/detail/{id}")
    CommonResult<Object> getDetail(@PathVariable Long id);

    // 批量查询商品（传入 ID 列表）
    @GetMapping("/batch")
    CommonResult<List<ProductDTO>> getByIds(@RequestParam("ids") List<Long> ids);

    // 扣减 SKU 库存
    @PostMapping("/sku/{skuId}/stock/deduct")
    CommonResult<Void> deductStock(@PathVariable Long skuId, @RequestParam Integer quantity);

    // 综合搜索（关键词 + 品牌 + 分类 + 排序）
    @GetMapping("/search")
    CommonResult<List<ProductDTO>> search(
        @RequestParam("keyword") String keyword,
        @RequestParam(value = "brandId", required = false) Long brandId,
        @RequestParam(value = "productCategoryId", required = false) Long productCategoryId,
        @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "sort", required = false) Integer sort
    );

    // 热门商品
    @GetMapping("/hot")
    CommonResult<List<ProductDTO>> getHotProducts(
        @RequestParam("pageNum") Integer pageNum,
        @RequestParam("pageSize") Integer pageSize
    );

    // 新品推荐
    @GetMapping("/new")
    CommonResult<List<ProductDTO>> getNewProducts(...);

    // 推荐商品
    @GetMapping("/recommend")
    CommonResult<List<ProductDTO>> getRecommendProducts(...);

    // 按品牌查询商品
    @GetMapping("/brand/{brandId}")
    CommonResult<List<ProductDTO>> getBrandProducts(@PathVariable Long brandId, ...);

    // 分类树
    @GetMapping("/categoryTreeList")
    CommonResult<List<Object>> categoryTreeList();

    // 子分类
    @GetMapping("/category/list/{parentId}")
    CommonResult<List<Object>> getCategoryList(@PathVariable Long parentId);

    // 推荐品牌
    @GetMapping("/brand/recommend")
    CommonResult<List<BrandDTO>> getRecommendBrands(
        @RequestParam("offset") Integer offset,
        @RequestParam("limit") Integer limit
    );
}
```

**接口特点**：
- 12 个方法覆盖了商品查询、库存扣减、搜索筛选、分类树、品牌推荐等完整能力
- 返回值统一使用 `CommonResult<T>` 包装
- 分页接口使用 `pageNum` + `pageSize` 传参
- `required = false` 的可选参数支持灵活组合查询条件

### 5.3 DefaultFeignConfig — Feign 全局配置

```java
public class DefaultFeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;  // 记录请求方法、URL、响应状态码、执行时间
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            // 传递 Authorization 头到下游服务（Token 透传，TODO）
        };
    }
}
```

**Feign 日志级别**：

| 级别 | 说明 |
|------|------|
| `NONE` | 不记录（默认） |
| `BASIC` | 请求方法、URL、响应状态码、执行时间 |
| `HEADERS` | BASIC + 请求和响应头 |
| `FULL` | HEADERS + 请求和响应体 |

选择 `BASIC` 是在日志信息的丰富度和性能之间取平衡。

---

## 六、DTO 对象详解

### 6.1 DTO 设计原则

"跨服务最小信息传输"——只包含其他服务需要的字段，不暴露完整实体。

```java
// ❌ 坏实践：直接暴露完整实体
CommonResult<PmsProduct> getById(Long id); // 40+ 字段全暴露

// ✅ 好实践：使用精简 DTO
CommonResult<ProductDTO> getById(Long id); // 只包含 7 个关键字段
```

### 6.2 ProductDTO

```java
@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String pic;
    private BigDecimal price;
    private Integer stock;
    private String brandName;
    private String productSn;
}
```

只包含展示商品卡片所需的 7 个字段，不包括 `detailHtml`（富文本）、`albumPics`（相册）等详情页才需要的字段。

### 6.3 各 DTO 字段对比

| DTO | 字段数 | 用途 |
|-----|--------|------|
| `ProductDTO` | 7 | 商品卡片展示 |
| `BrandDTO` | 5 | 品牌展示 |
| `CartItemDTO` | 13 | 购物车项 |
| `MemberDTO` | 11 | 会员信息 |
| `SubjectDTO` | 4 | 专题卡片 |
| `StockDeductDTO` | 3 | 扣减库存请求 |

---

## 七、Feign 调用数据流

### 7.1 调用链路示例（以首页加载为例）

```
浏览器请求: /home/content
     │
     v
 portal-service (BFF 聚合层)
     │
     ├──── ProductClient.getHotProducts()       ──→ product-service /product/hot
     ├──── ProductClient.getNewProducts()       ──→ product-service /product/new
     ├──── ProductClient.getRecommendBrands()   ──→ product-service /brand/recommend
     ├──── MarketingClient.getHomeFlashPromotion() ──→ marketing-service /flash/home
     ├──── HomeClient.getHomeAdvertises()       ──→ marketing-service /home/advertise/list
     └──── SubjectClient.listAll()              ──→ marketing-service /subject/listAll
```

### 7.2 调用链路示例（以下单为例）

```
用户请求: POST /order/generateOrder
     │
     v
 trade-service
     │
     ├──── CartClient.list()         ──→ cart-service /cart/list（获取购物车）
     ├──── MemberClient.getById()    ──→ member-service /member/{id}（获取会员）
     ├──── ProductClient.deductStock()──→ product-service /product/sku/{id}/stock/deduct
     └──（回调）支付成功后更新商品销量
```

---

## 八、核心设计亮点总结

1. **集中管理**：所有 Feign 接口定义在一个模块，便于查找和维护
2. **避免循环依赖**：mall-api 是纯接口模块，不依赖任何业务模块，所以各业务模块可以安全依赖它
3. **跨服务最小传输**：DTO 只包含必要字段，减少网络传输量
4. **统一返回格式**：所有 Feign 返回值统一使用 `CommonResult<T>`，前端/调用方无需处理不同格式
5. **Feign 配置集中**：`DefaultFeignConfig` 统一配置日志级别和 Token 透传
6. **接口即合约**：Feign 接口是微服务之间的 API 合约，定义清晰后，消费者和提供者可以并行开发
7. **版本隔离**：mall-api 的改动反映的是 API 层面的变化，不影响各服务内部实现

---

---

# 第二部分：Java 面试 Feign 高频问题

## 面试题 1：OpenFeign 的工作原理是什么？

**核心机制：动态代理 + HTTP 客户端**。

```
调用方: productClient.getById(1L)
     │
     v
JDK 动态代理 (FeignInvocationHandler)
     │
     v
解析方法上的注解 (@GetMapping/@PathVariable 等)
     │
     ├─ 拼接 URL: http://product-service/product/1
     ├─ 构造请求参数
     │
     v
通过 LoadBalancer 选择目标实例
     │
     v
HttpClient (默认 HttpURLConnection) 发送 HTTP 请求
     │
     v
接收响应，反序列化为 CommonResult<ProductDTO>
```

**关键类**：
- `Feign.Builder` — 构建 Feign 客户端
- `ReflectiveFeign` — 解析接口注解生成动态代理
- `SynchronousMethodHandler` — 处理每次方法调用的请求/响应
- `LoadBalancerFeignClient` — 集成负载均衡

**面试回答建议**：
> Feign 基于 JDK 动态代理，为每个 `@FeignClient` 接口生成一个代理对象。调用接口方法时，代理拦截方法调用，解析路径、参数、请求方法等元信息，拼接出完整的 HTTP 请求，通过内置的 HTTP 客户端发送到目标服务，再将响应反序列化为返回类型。在 Spring Cloud 中，它还集成了 LoadBalancer 实现负载均衡。

---

## 面试题 2：Feign 如何实现负载均衡？

Feign 通过 `LoadBalancerFeignClient` 集成 Spring Cloud LoadBalancer：

```java
@FeignClient(name = "product-service", path = "/product")
public interface ProductClient { ... }
```

`name = "product-service"` 并非直接 DNS 解析，而是：
1. Feign 识别到服务名 `product-service`
2. 交给 `LoadBalancerFeignClient` 处理
3. 通过 `DiscoveryClient`（Nacos）获取 `product-service` 的所有实例 IP:Port
4. 按负载均衡策略（默认轮询）选择一个实例
5. 将 URL 中的服务名替换为选中的 `IP:Port`，如 `http://product-service/product/1` → `http://192.168.1.101:8082/product/1`

**面试回答建议**：
> Feign 本身不负责负载均衡，它通过 Spring Cloud LoadBalancer 集成 Nacos 服务发现。当 `@FeignClient` 的 `name` 是服务名时，LoadBalancer 从 Nacos 获取该服务的实例列表，按策略选择一个，ReactiveLoadBalancer 默认使用轮询策略。

---

## 面试题 3：Feign 的 RequestInterceptor 有什么作用？

**典型场景**：在微服务调用链中传递认证 Token。

```java
@Bean
public RequestInterceptor requestInterceptor() {
    return template -> {
        // 从当前请求上下文获取 Token
        String token = RequestContextHolder.currentRequestAttributes()
            .getAttribute("token", 0);
        // 放入 Feign 请求头
        template.header("Authorization", "Bearer " + token);
    };
}
```

**使用场景**：

| 场景 | 说明 |
|------|------|
| Token 透传 | 上游请求的 JWT Token 自动传给下游 |
| 链路追踪 | 传递 TraceId、SpanId（如 SkyWalking） |
| 灰度标记 | 传递灰度版本号，控制路由 |
| 通用请求头 | 添加版本号、客户端标识等 |

**面试回答建议**：
> `RequestInterceptor` 是 Feign 的请求拦截器，在每次 Feign 调用发起 HTTP 请求之前执行。最常见的是 Token 透传——从上游 HTTP 请求中取出认证 Token，设置到 Feign 请求的 `Authorization` 头，这样 Token 就能沿着微服务调用链传递下去。

---

## 面试题 4：mall-api 为什么把 Feign 接口集中管理？

**传统方式的问题**：
```
product-service ──需要调用──→ member-service
     │                           │
     └── 依赖 member-service 的 jar ──→ 循环依赖风险
```

如果 product-service 的 jar 中包含了 member-service 的 Feign 接口，而 member-service 也可能需要调用 product-service，就会产生**循环依赖**。

**mall-api 的解决方案**：
```
mall-api (纯接口模块，不依赖任何业务模块)
    ↑                 ↑
    │                 │
product-service   member-service
```

**面试回答建议**：
> 将 Feign 接口抽取到独立的 API 模块有三个好处：一是避免微服务间的循环依赖，因为 API 模块不依赖任何业务模块；二是接口即合约，API 模块的变更可以作为版本管理的依据；三是消费者和提供者可以基于接口定义并行开发，提高团队效率。

---

## 面试题 5：Feign 的日志级别如何配置？

```yaml
# application.yml
logging:
  level:
    com.mall.api.client.ProductClient: DEBUG  # 必须设为 DEBUG 才能看到 Feign 日志

feign:
  client:
    config:
      default:
        loggerLevel: BASIC  # 或 FULL
```

**注意**：
- Feign 日志级别 ≠ Logback 日志级别
- Feign 的 `Logger.Level` 控制记录哪些内容
- Logback 的 `logging.level` 控制是否输出（必须设 DEBUG）

**面试回答建议**：
> Feign 日志需要两步配置：第一步，在 Feign 配置中设置 `Logger.Level`（NONE/BASIC/HEADERS/FULL）；第二步，在 Logback 中将对应的 Feign 接口包路径的日志级别设为 DEBUG，否则 Feign 日志不会真正输出。

---

## 面试题 6：Feign 调用超时怎么处理？

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connectTimeout: 5000   # 连接超时 5s
            readTimeout: 10000     # 读取超时 10s
          product-service:
            readTimeout: 30000     # 商品服务特殊配置 30s
```

**超时层级**：`default` 为全局配置，`服务名` 为特定服务覆盖配置。

---

## 面试速查表

| 问题 | 一句话答案 |
|------|-----------|
| Feign 原理 | JDK 动态代理 + 注解解析 + HTTP 客户端 |
| 负载均衡 | LoadBalancerFeignClient + Nacos 服务发现 |
| RequestInterceptor | 每次 HTTP 请求前执行，常用于 Token 透传 |
| mall-api 设计 | 集中接口避免循环依赖，接口即合约 |
| Feign 日志 | Logger.Level 控制内容，logback level 控制输出 |
| 超时配置 | connectTimeout + readTimeout，可按服务配置 |
| 返回格式 | 统一 CommonResult<T>，与 Spring MVC 保持一致 |

---

*学习日期：2026-07-08*
