# mall-cloud 购物车服务学习笔记

---

# 第一部分：cart-service 模块完整梳理

## 一、模块定位

`cart-service` 是 mall-cloud 微服务电商平台的 **前台购物车服务**，负责 C 端用户的购物车管理：添加商品、修改数量、变更规格、获取购物车列表（含促销信息计算）、删除和清空。购物车服务通过 Feign 调用 member-service（获取当前会员）和 marketing-service（计算促销价格），实现了跨服务的业务协作。

---

## 二、技术架构全景

```
             mall-gateway (:/mall-portal/**)
                        │
                        v
                mall-auth (会员认证)
                        │
                        v
    ┌───────────────────────────────────────┐
    │          cart-service :8086          │
    │          (购物车服务)                  │
    │                                       │
    │  ┌─────────────────────────────────┐  │
    │  │ CartController               ★ │  │
    │  │ POST /cart/add                │  │  ← 添加商品到购物车
    │  │ GET  /cart/list               │  │  ← 购物车列表
    │  │ GET  /cart/list/promotion     │  │  ← 含促销的购物车 ★
    │  │ POST /cart/delete             │  │  ← 删除
    │  │ POST /cart/clear              │  │  ← 清空
    │  │ GET  /cart/getProduct/{id}    │  │  ← 获取商品规格
    │  └─────────────────────────────────┘  │
    │                                       │
    │  Feign 调用:                           │
    │  → member-service:  获取当前会员       │
    │  → marketing-service: 计算促销价格     │
    │                                       │
    │  数据库: mall_cart (MySQL)            │
    │  缓存: Redis                          │
    └───────────────────────────────────────┘
```

---

## 三、目录结构

```
cart-service/
├── pom.xml
└── src/main/
    ├── java/com/mall/cart/
    │   ├── CartApplication.java         # 启动类
    │   ├── config/
    │   │   └── MyBatisConfig.java       # MyBatis 配置
    │   ├── controller/
    │   │   └── CartController.java      # ★ 购物车控制器
    │   ├── domain/dto/
    │   │   ├── CartProduct.java         # 商品规格选择 DTO
    │   │   └── CartPromotionItem.java   # 促销购物车项 DTO
    │   ├── feign/
    │   │   ├── OmsPromotionService.java # Feign → marketing-service
    │   │   └── UmsMemberService.java    # Feign → member-service
    │   ├── mapper/
    │   │   ├── OmsCartItemMapper.java
    │   │   └── PortalProductMapperCustom.java # 跨库查询商品
    │   ├── model/
    │   │   ├── OmsCartItem.java         # 购物车项
    │   │   ├── PmsProduct.java          # 商品实体（跨服务复制）
    │   │   ├── PmsProductAttribute.java
    │   │   ├── PmsSkuStock.java
    │   │   └── UmsMember.java           # 会员实体（跨服务复制）
    │   └── service/
    │       ├── ICartService.java
    │       └── impl/
    │           └── CartServiceImpl.java  ★
    └── resources/
        ├── application.yml
        ├── application-dev.yml
        └── mapper/
            ├── OmsCartItemMapper.xml
            └── PortalProductMapperCustom.xml # 跨库查询商品信息
```

---

## 四、核心功能详解

### 4.1 CartController — 购物车 API

| API | 方法 | 路径 | 说明 |
|-----|------|------|------|
| add | POST | /cart/add | 添加商品到购物车 |
| list | GET | /cart/list | 获取购物车列表（不含促销） |
| listPromotion | GET | /cart/list/promotion | 获取含促销的购物车 ★ |
| updateQuantity | GET | /cart/update/quantity | 修改商品数量 |
| getCartProduct | GET | /cart/getProduct/{productId} | 获取商品规格信息 |
| updateAttr | POST | /cart/update/attr | 修改商品规格 |
| delete | POST | /cart/delete | 删除购物车商品 |
| clear | POST | /cart/clear | 清空购物车 |

### 4.2 添加商品到购物车 — 核心逻辑

```java
@Transactional
public int add(OmsCartItem cartItem) {
    // 1. 通过 Feign 获取当前登录会员
    UmsMember currentMember = memberService.getCurrentMember();
    cartItem.setMemberId(currentMember.getId());
    cartItem.setMemberNickname(currentMember.getNickname());
    cartItem.setDeleteStatus(0);

    // 2. 检查是否已存在相同的商品+SKU
    OmsCartItem existCartItem = getCartItem(cartItem);
    if (existCartItem == null) {
        // 不存在 → 新增
        cartItem.setCreateDate(new Date());
        return cartItemMapper.insert(cartItem);
    } else {
        // 已存在 → 累加数量
        existCartItem.setQuantity(existCartItem.getQuantity() + cartItem.getQuantity());
        return cartItemMapper.updateByPrimaryKey(existCartItem);
    }
}
```

### 4.3 修改规格 — 先删后增

```java
@Transactional
public int updateAttr(OmsCartItem cartItem) {
    // 1. 软删除旧记录
    OmsCartItem updateCart = new OmsCartItem();
    updateCart.setDeleteStatus(1);
    cartItemMapper.updateByPrimaryKeySelective(updateCart);
    // 2. 创建新记录（注意 ID 置 null）
    cartItem.setId(null);
    return add(cartItem);
}
```

**为什么不用 UPDATE**：规格变更后相当于一个全新的购物车项（不同 SKU），价格、属性等都不同，所以删除旧记录并创建新记录比修改所有字段更简洁。

### 4.4 含促销的购物车列表

```java
public List<CartPromotionItem> listPromotion(Long memberId, List<Long> cartIds) {
    // 1. 获取购物车列表
    List<OmsCartItem> cartItemList = list(memberId);
    if (cartIds != null && !cartIds.isEmpty()) {
        cartItemList = cartItemList.stream()
            .filter(item -> cartIds.contains(item.getId()))
            .collect(Collectors.toList());
    }
    // 2. 调用 marketing-service 计算促销
    return promotionService.calcCartPromotion(cartItemList);
}
```

**促销信息字段** (`CartPromotionItem extends OmsCartItem`):
- `promotionMessage`: 促销文案（如"限时特惠"）
- `reduceAmount`: 促销减免金额
- `realStock`: 真实库存（考虑促销库存）
- `integration`: 赠送积分
- `growth`: 赠送成长值

### 4.5 跨服务读库设计

cart-service 直接查询 product-service 的数据库表：

```sql
-- PortalProductMapperCustom.xml
-- 查询商品时带出属性列表和 SKU 列表
<resultMap id="cartProductMap" extends="productResultMap">
    <collection property="productAttributeList"
        column="product_attribute_category_id"
        select="selectProductAttributeList"/>
    <collection property="skuStockList"
        column="id"
        select="selectSkuStockList"/>
</resultMap>
<select id="getCartProduct" resultMap="cartProductMap">
    SELECT * FROM pms_product WHERE id = #{id}
</select>
```

**注意**：这里使用了嵌套 SELECT（N+1 查询），存在 N+1 问题，但购物车场景中商品数量有限，影响不大。

### 4.6 软删除设计

```java
// 删除购物车项 → 设置 deleteStatus = 1
cartItem.setDeleteStatus(1);
cartItemMapper.updateByPrimaryKeySelective(cartItem);

// 清空购物车 → 批量软删除
OmsCartItem record = new OmsCartItem();
record.setDeleteStatus(1);
OmsCartItemExample example = new OmsCartItemExample();
example.createCriteria().andMemberIdEqualTo(memberId);
cartItemMapper.updateByExampleSelective(record, example);
```

---

## 五、数据库表结构

**oms_cart_item**（购物车项）：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| product_id | bigint | 商品 ID |
| product_sku_id | bigint | SKU ID |
| member_id | bigint | 会员 ID |
| quantity | int | 购买数量 |
| price | decimal | 加入时的价格 |
| product_pic | varchar | 商品图片 |
| product_name | varchar | 商品名称 |
| product_sku_code | varchar | SKU 编码 |
| member_nickname | varchar | 会员昵称（冗余） |
| product_attr | varchar | 商品销售属性（JSON） |
| delete_status | int | 软删除标记（0=正常，1=已删除） |
| create_date | datetime | 创建时间 |
| modify_date | datetime | 修改时间 |

---

## 六、核心设计亮点总结

1. **累加策略**：同一商品同一 SKU 重复添加时累加数量而非创建新记录
2. **先删后增**：规格变更时删除旧记录创建新记录，逻辑清晰
3. **促销分离**：购物车只存基础数据，促销计算委托给 marketing-service
4. **跨服务读库**：商品/SKU 信息直接查 product-service 的数据库，避免多次 Feign 调用
5. **会员 ID 统一来源**：从 Feign 获取当前会员，保证数据一致性
6. **软删除**：deleteStatus 标记删除，数据可恢复

---

---

# 第二部分：Java 面试购物车服务高频问题

## 面试题 1：购物车的"累加策略"如何避免并发问题？

```java
// 读→判断→写 三步操作存在并发问题
existCartItem = getCartItem(cartItem);       // 1. 查询
if (existCartItem == null) {                 // 2. 判断
    insert(cartItem);                        // 3. 写入
} else {
    existCartItem.setQuantity(existCartItem.getQuantity() + cartItem.getQuantity());
    updateByPrimaryKey(existCartItem);       // 3. 更新
}
```

**解决方案**：
- 简单方案：数据库唯一索引 `(member_id, product_id, product_sku_id, delete_status)` 防止重复插入
- 更优方案：`INSERT ... ON DUPLICATE KEY UPDATE quantity = quantity + #{quantity}`

**面试回答建议**：
> 累加策略的核心问题是并发场景下"查→不存在→插入"这三步不在同一个原子操作中。解决方案一是在 member_id + product_id + sku_id + delete_status 上建唯一索引，重复插入会触发唯一约束异常，捕获后执行累加。更好的做法是用 MySQL 的 `ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)` 一条 SQL 原子完成。

---

## 面试题 2：购物车删除为什么用软删除？

三个原因：
1. **数据恢复**：用户误删可以恢复（如"撤销删除"功能）
2. **数据分析**：可以分析"添加到购物车但未下单"的商品，优化转化率
3. **避免外键问题**：如果订单表引用了购物车 ID，物理删除会导致数据不一致

**面试回答建议**：
> 软删除在电商场景中很有价值——通过分析被删除的购物车项，可以了解用户为什么放弃购买（价格太高？规格不合适？），用于优化商品策略。技术上，软删除也避免了外键约束和数据丢失的问题。代价是所有查询都要加 `delete_status=0` 的过滤条件。

---

## 面试题 3：跨服务直接读库 vs Feign 调用，如何选择？

| 维度 | 直接读库 | Feign 调用 |
|------|---------|-----------|
| 性能 | 一次 SQL | 一次 HTTP + 一次 SQL |
| 数据一致性 | 可能读到旧数据（缓存不一致） | 始终读到最新数据 |
| 服务边界 | 打破微服务边界 | 保持边界清晰 |
| 事务 | 不在同一事务 | 不在同一事务 |
| 耦合度 | 高 | 低 |

**本项目的选择**：商品规格信息通过直接读库获取（性能优先），促销信息通过 Feign 调用（数据一致性优先）。

**面试回答建议**：
> 微服务最佳实践是通过 Feign/API 调用，保持服务边界清晰。但读场景下，如果数据变更频率低且对一致性要求不高，直接读库可以避免一次网络往返。cart-service 选择直接读 product 数据库是折中方案，在开发阶段简化了实现。生产环境建议统一用 Feign 调用，保持架构清晰。

---

## 面试速查表

| 问题 | 一句话答案 |
|------|-----------|
| 累加策略 | 同商品同SKU → quantity+=N，否则新增 |
| 软删除 | deleteStatus=1 标记，数据可恢复可分析 |
| 规格变更 | 先删后增策略，简洁清晰 |
| 促销计算 | 委托 marketing-service /promotion/calcCartPromotion |
| 跨服务读库 | 牺牲边界清晰度换取性能，生产建议用 Feign |

---

*学习日期：2026-07-08*
