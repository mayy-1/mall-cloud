# mall-cloud 会员服务学习笔记

---

# 第一部分：member-service 模块完整梳理

## 一、模块定位

`member-service` 是 mall-cloud 微服务电商平台的 **前台会员服务**，负责 C 端用户的注册、登录、个人信息管理、收货地址管理、优惠券领取与使用、品牌关注、商品收藏、浏览历史等功能。与 `user-service`（后台管理员）形成双账号体系的前台一侧。

---

## 二、技术架构全景

```
           mall-gateway (:/mall-portal/**)
                    │
                    v
            mall-auth (认证路由 → portal-app)
                    │
                    ▼
    ┌─────────────────────────────────────────┐
    │         member-service :8085            │
    │         (前台会员服务)                     │
    │                                         │
    │  ┌──────────────────────────────────┐   │
    │  │  MemberController               │   │  ← 会员 CRUD + 登录
    │  │  POST /sso/login  (JWT 模式)     │   │
    │  │  POST /sso/register             │   │
    │  │  POST /sso/getAuthCode          │   │
    │  └──────────────────────────────────┘   │
    │  ┌──────────────────────────────────┐   │
    │  │  MemberFeignController          │   │  ← Feign 内部调用接口
    │  │  GET  /member/current           │   │
    │  │  GET  /member/{id}              │   │
    │  └──────────────────────────────────┘   │
    │  ┌──────────────────────────────────┐   │
    │  │  AddressController              │   │  ← 收货地址 CRUD
    │  │  CouponController               │   │  ← 优惠券领取/使用
    │  │  AttentionController            │   │  ← 品牌关注
    │  │  ProductCollectionController    │   │  ← 商品收藏
    │  │  ReadHistoryController          │   │  ← 浏览历史
    │  └──────────────────────────────────┘   │
    │                                         │
    │  数据库: mall_member (MySQL)            │
    │  MongoDB: mall-port (品牌关注/收藏/历史)  │
    │  认证: Sa-Token (JWT 模式)               │
    └─────────────────────────────────────────┘
```

---

## 三、目录结构

```
member-service/
├── pom.xml
└── src/main/
    ├── java/com/mym/mall/member/
    │   ├── MallMemberApplication.java    # 启动类
    │   ├── config/
    │   │   └── SaTokenMemberConfig.java  # Sa-Token JWT 配置 ★
    │   ├── controller/
    │   │   ├── MemberController.java             # 会员管理 ★
    │   │   ├── MemberFeignController.java        # Feign 内部接口
    │   │   ├── AddressController.java            # 收货地址
    │   │   ├── CouponController.java             # 优惠券
    │   │   ├── AttentionController.java          # 品牌关注
    │   │   ├── ProductCollectionController.java  # 商品收藏
    │   │   └── ReadHistoryController.java        # 浏览历史
    │   ├── domain/
    │   │   ├── MemberDetails.java                # 会员详情封装
    │   │   ├── MemberBrandAttention.java         # 品牌关注 (MongoDB 文档)
    │   │   ├── MemberProductCollection.java      # 商品收藏 (MongoDB 文档)
    │   │   └── MemberReadHistory.java            # 浏览历史 (MongoDB 文档)
    │   ├── mapper/ (17个 Mapper 接口)
    │   ├── model/ (17个实体类)
    │   ├── repository/ (3个 MongoDB Repository)
    │   ├── service/ (8个 Service 接口 + 实现)
    │   └── util/
    │       └── StpMemberUtil.java       # 会员认证工具类
    └── resources/
        ├── application.yml
        ├── application-dev.yml
        └── mapper/ (15个 MyBatis XML)
```

---

## 四、核心依赖解读（pom.xml）

| 依赖 | 作用 |
|------|------|
| `mall-common` | 公共模块 |
| `mall-api` | Feign 接口模块 |
| `spring-boot-starter-web` | Web 服务 |
| `mybatis-spring-boot-starter` + `druid` + `mysql` | 数据库访问 |
| `pagehelper-spring-boot-starter` | 分页 |
| `spring-boot-starter-data-mongodb` | **MongoDB** — 存储关注/收藏/历史 |
| `spring-boot-starter-data-redis` | Redis |
| `sa-token-spring-boot3-starter` | Sa-Token 认证 |
| `sa-token-jwt` | **JWT 模式** — 会员采用无状态 JWT |
| `sa-token-redis-jackson` | Sa-Token Redis 集成 |
| `knife4j-openapi3-jakarta-spring-boot-starter` | API 文档 |

---

## 五、核心功能详解

### 5.1 双账号体系对比

| 维度 | user-service (管理员) | member-service (会员) |
|------|---------------------|---------------------|
| 认证工具 | `StpUtil`（默认） | `StpMemberUtil`（自定义） |
| Token 模式 | Redis Session | **JWT** |
| 密码存储 | BCrypt | BCrypt |
| 权限体系 | RBAC（角色/菜单/资源） | 仅登录认证 |
| 数据库 | mall_user | mall_member |
| 额外存储 | 无 | MongoDB |

### 5.2 SaTokenMemberConfig — JWT 模式配置 ★

```java
@Configuration
public class SaTokenMemberConfig {
    @Bean
    public StpLogic stpLogicForMember() {
        return new StpLogicJwtForSimple("memberLogin"); // JWT 模式
    }
}
```

**关键点**：会员认证使用 `StpLogicJwtForSimple`（JWT 简单模式），Token 直接包含用户信息，无需查 Redis。这与管理员使用 Redis Session 模式形成对比：

- **会员 JWT**：无状态，移动端友好，高性能，但无法主动吊销
- **管理员 Redis Session**：有状态，支持强制下线，可灵活控制权限

### 5.3 StpMemberUtil — 会员认证工具

```java
public class StpMemberUtil {
    public static final String TYPE = "memberLogin";

    public static StpLogic stpLogic = new StpLogicJwtForSimple(TYPE);

    // 登录
    public static void login(Object id) { stpLogic.login(id); }

    // 检查登录
    public static void checkLogin() { stpLogic.checkLogin(); }

    // 获取当前会员ID
    public static long getLoginIdAsLong() { ... }

    // 注销
    public static void logout() { stpLogic.logout(); }
}
```

**设计模式**：静态工具类封装 Sa-Token API。网关通过 `StpMemberUtil.checkLogin()` 校验前台请求。

### 5.4 MemberController — 会员核心接口

| API | 方法 | 路径 | 说明 |
|-----|------|------|------|
| login | POST | /sso/login | 会员登录，返回 JWT Token |
| register | POST | /sso/register | 会员注册 |
| getAuthCode | POST | /sso/getAuthCode | 获取短信验证码 |
| updatePassword | POST | /sso/updatePassword | 修改密码 |
| refreshToken | POST | /sso/refreshToken | 刷新 Token |
| getItem | GET | /member/{id} | 获取会员信息 |
| getCurrentMember | GET | /member/current | 获取当前登录会员 |
| update | POST | /member/update | 修改个人信息 |

**登录实现**：
```java
// 1. 验证用户名密码
UmsMember member = memberMapper.selectByUsername(username);
if (!BCrypt.checkpw(password, member.getPassword())) {
    Asserts.fail("密码不正确");
}
// 2. JWT Token 生成
StpMemberUtil.login(member.getId());
String token = StpMemberUtil.getTokenValue();
// 3. 返回 Token
Map<String, String> tokenMap = new HashMap<>();
tokenMap.put("token", token);
tokenMap.put("tokenHead", "Bearer ");
```

### 5.5 优惠券服务 — CouponController

| API | 路径 | 说明 |
|-----|------|------|
| list | /member/coupon/list | 获取会员优惠券列表 |
| add | /member/coupon/add/{couponId} | 会员领取优惠券 |
| listCart | /member/coupon/list/cart/{type} | 获取匹配购物车的优惠券 |
| listHistory | /member/coupon/listHistory | 优惠券使用历史 |

**优惠券使用类型判断** (`useType`)：

```java
// useType = 0: 全场通用 → 所有商品可用
// useType = 1: 指定分类 → 商品分类匹配即可用
// useType = 2: 指定商品 → 仅限指定商品
// 计算优惠金额时：
// 全场券: 对所有商品生效
// 分类券: productCategoryId 匹配
// 商品券: productId 匹配
```

### 5.6 MongoDB 文档存储

member-service 使用 MongoDB 存储三类用户行为数据：

```java
// 品牌关注
@Document("memberBrandAttention")
public class MemberBrandAttention {
    private String id;
    private Long memberId;
    private Long brandId;
    private String brandName;
    private String brandLogo;
    private Date createTime;
}

// 商品收藏
@Document("memberProductCollection")
public class MemberProductCollection {
    private Long memberId;
    private Long productId;
    private String productName;
    private String productPic;
    private Date createTime;
}

// 浏览历史
@Document("memberReadHistory")
public class MemberReadHistory {
    private Long memberId;
    private Long productId;
    private String productName;
    private String productPic;
    private Date createTime;
}
```

**为什么用 MongoDB 而不是 MySQL**：
- 这些是"用户行为日志"类数据，写入频繁、查询简单
- MongoDB 的文档模型天然适合（每个用户一个文档或一条记录）
- 不需要复杂的关联查询
- 高写入吞吐量，对一致性要求不高

---

## 六、核心设计亮点总结

1. **双认证体系**：管理员 Redis Session vs 会员 JWT，各取所需
2. **静态工具类封装**：`StpMemberUtil` 将 Sa-Token API 封装为静态方法，调用简洁
3. **混合存储**：MySQL（核心业务）+ MongoDB（行为日志），各尽其能
4. **优惠券智能匹配**：useType 字段区分全场/分类/商品，配合购物车计算促销
5. **密码安全**：BCrypt 加密存储，注册和登录都做密码校验
6. **Feign 双向接口**：对外暴露 `/sso/login`（前端调用），对内暴露 `/member/current`（其他服务 Feign 调用）

---

---

# 第二部分：Java 面试会员服务高频问题

## 面试题 1：会员为什么使用 JWT 而管理员使用 Redis Session？

| 维度 | 会员 (JWT) | 管理员 (Redis Session) |
|------|-----------|----------------------|
| 用户量 | 海量（百万级） | 少量（几十个） |
| 性能要求 | 高并发无状态 | 可接受 Redis 查询 |
| 吊销需求 | 少（很少需要强制下线） | 多（管理员权限控制灵活） |
| 终端类型 | App/小程序/H5 | 浏览器 Web |
| Token 携带 | 复杂终端环境 | 标准 HTTP Header |

**面试回答建议**：
> 会员使用 JWT 是因为 C 端用户量大、并发高，JWT 的无状态特性避免每次请求都查 Redis，性能更好。管理员用 Redis Session 是因为管理员需要灵活的权限控制和强制下线能力，且管理员数量少，Redis 压力可接受。这是典型的"用空间换控制"策略。

---

## 面试题 2：为什么品牌关注/商品收藏/浏览历史用 MongoDB？

三个原因：
1. **数据结构简单**：每条记录都是独立的文档（谁关注了什么品牌），不需要多表 JOIN
2. **写入量大**：浏览历史属于高频写入，MongoDB 的写入性能优于 MySQL
3. **扩展性好**：MongoDB 天然支持分片，用户量增长时可以横向扩展

**面试回答建议**：
> 用户行为数据的特点是写入频繁、查询简单、数据量大。MongoDB 的文档模型不需预定义 Schema，水平扩展容易，非常适合存储这类数据。比如浏览历史，每次浏览都产生一条记录，用 MySQL 会有写入瓶颈，用 MongoDB + 索引可以支撑更高的 QPS。

---

## 面试题 3：优惠券的 useType 三种模式有什么区别？

```java
// useType = 0 (全场通用)
// 判断逻辑：购物车总金额 >= 使用门槛 (minPoint) 即可

// useType = 1 (指定分类)
// 判断逻辑：购物车中属于指定分类的商品金额总和 >= minPoint

// useType = 2 (指定商品)
// 判断逻辑：购物车中包含指定商品即可使用
```

结算时匹配优惠券的算法：
1. 遍历会员持有的所有未使用优惠券
2. 按 useType 分别调用不同的匹配逻辑
3. 匹配成功的优惠券按金额降序排列
4. 用户选择一张优惠券使用

---

## 面试速查表

| 问题 | 一句话答案 |
|------|-----------|
| 会员 JWT | 无状态高性能，适合海量 C 端用户 |
| 管理员 Session | 有状态可控，适合后台管理 |
| MongoDB | 用户行为日志存储，高写入性能 |
| 优惠券 useType | 0全场/1分类/2商品，三级匹配逻辑 |
| StpMemberUtil | 静态工具类封装 Sa-Token 会员 API |
| BCrypt | 密码不可逆加密，每次生成的哈希不同 |

---

*学习日期：2026-07-08*
