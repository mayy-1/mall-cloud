# mall-cloud 订单服务学习笔记

---

# 第一部分：order-service 模块完整梳理

## 一、模块定位

`order-service` 是 mall-cloud 微服务电商平台的 **后台订单管理服务**，负责订单的查询、发货、关闭、删除（软删除）、费用修改、收货人信息修改等后台管理操作。同时管理退货申请、退货原因和公司发货地址。它是后台运营人员处理订单的核心工具。

---

## 二、技术架构全景

```
          mall-gateway (:/mall-admin/**)
                    │
                    v
              mall-auth (管理员认证)
                    │
                    ▼
    ┌──────────────────────────────────┐
    │      order-service :8083        │
    │      (后台订单管理服务)           │
    │                                  │
    │  ┌────────────────────────────┐  │
    │  │ OrderController     ★★    │  │  ← 订单查询/发货/关闭/删除
    │  │ OrderSettingController    │  │  ← 订单设置（超时时间）
    │  │ CompanyAddressController  │  │  ← 公司发货地址管理
    │  │ ReturnApplyController     │  │  ← 退货申请处理
    │  │ ReturnReasonController    │  │  ← 退货原因管理
    │  └────────────────────────────┘  │
    │                                  │
    │  数据库: mall_order (MySQL)       │
    │  连接池: Druid                    │
    │  分页: PageHelper                 │
    └──────────────────────────────────┘
```

**注意**：order-service 负责**后台订单管理**，而 **前台用户下单** 由 `trade-service` 负责。两者配合完成完整的订单生命周期。

---

## 三、目录结构

```
order-service/
├── pom.xml
└── src/main/
    ├── java/com/mall/order/
    │   ├── OrderApplication.java          # 启动类
    │   ├── config/
    │   │   └── MyBatisConfig.java         # MyBatis + 事务管理
    │   ├── controller/
    │   │   ├── OrderController.java       # ★ 订单管理核心
    │   │   ├── OrderSettingController.java
    │   │   ├── CompanyAddressController.java
    │   │   ├── ReturnApplyController.java
    │   │   └── ReturnReasonController.java
    │   ├── domain/dto/
    │   │   ├── OmsOrderQueryParam.java    # 订单查询参数
    │   │   ├── OmsOrderDetail.java        # 订单详情（含商品+操作记录）
    │   │   ├── OmsOrderDeliveryParam.java # 发货参数
    │   │   ├── OmsReceiverInfoParam.java  # 收货人信息
    │   │   └── OmsMoneyInfoParam.java     # 费用信息
    │   ├── mapper/ (11个 Mapper 接口)
    │   │   ├── OmsOrderMapperCustom.java  # 自定义订单查询 ★
    │   │   └── ...
    │   ├── model/ (15个实体类)
    │   └── service/ (5个 Service 接口 + 实现)
    └── resources/
        ├── application.yml
        ├── application-dev.yml
        └── mapper/ (11个 MyBatis XML)
            ├── OmsOrderMapperCustom.xml   # ★ CASE WHEN 批量发货
            └── ...
```

---

## 四、核心功能详解

### 4.1 订单状态机

```
     创建订单
         │
         v
    ┌─────────┐    支付成功    ┌─────────┐    发货     ┌─────────┐   确认收货   ┌─────────┐
    │ 0:待付款 │ ──────────→ │ 1:待发货 │ ─────────→ │ 2:已发货 │ ─────────→ │ 3:已完成 │
    └─────────┘              └─────────┘            └─────────┘           └─────────┘
         │                       │
         │    取消/超时           │    取消/退款
         v                       v
    ┌──────────────────────────────────────┐
    │              4:已关闭                  │
    └──────────────────────────────────────┘
```

### 4.2 OrderController — 订单管理 API

| API | 方法 | 路径 | 说明 |
|-----|------|------|------|
| list | GET | /order/list | 分页查询订单（多条件筛选） |
| delivery | POST | /order/update/delivery | 批量发货 ★ |
| close | POST | /order/update/close | 批量关闭订单 |
| delete | POST | /order/delete | 批量删除（软删除） |
| detail | GET | /order/{id} | 订单详情（含商品+操作记录） |
| updateReceiverInfo | POST | /order/update/receiverInfo | 修改收货人信息 |
| updateMoneyInfo | POST | /order/update/moneyInfo | 修改费用（运费/折扣） |
| updateNote | POST | /order/update/note | 修改备注 |

### 4.3 批量发货 — CASE WHEN 优化

```sql
-- OmsOrderMapperCustom.xml
<update id="delivery">
    UPDATE oms_order
    SET delivery_sn = CASE id
        <foreach collection="list" item="item">
            WHEN #{item.orderId} THEN #{item.deliverySn}
        </foreach>
        END,
        delivery_company = CASE id
        <foreach collection="list" item="item">
            WHEN #{item.orderId} THEN #{item.deliveryCompany}
        </foreach>
        END,
        delivery_time = NOW(),
        status = 2
    WHERE id IN <foreach collection="list" item="item" open="(" separator="," close=")">#{item.orderId}</foreach>
    AND status = 1  -- 只能对待发货的订单进行发货
    AND delete_status = 0
</update>
```

**优点**：一条 SQL 完成所有订单的发货，避免了逐条 UPDATE 的性能问题。

### 4.4 操作历史记录

每次重要操作后插入 `oms_order_operate_history` 记录：

```java
// 发货后记录
OmsOrderOperateHistory history = new OmsOrderOperateHistory();
history.setOrderId(order.getId());
history.setOperateMan("后台管理员");
history.setOrderStatus(2);       // 状态变为已发货
history.setNote("完成发货");
history.setCreateTime(new Date());
operateHistoryMapper.insert(history);
```

这意味着每个订单都有完整的操作审计链：谁在什么时间做了什么操作。

### 4.5 订单详情 — 三表联查

```sql
-- OmsOrderMapperCustom.xml
<resultMap id="orderDetailResultMap" extends="BaseResultMap">
    <collection property="orderItemList"
        columnPrefix="item_"
        resultMap="OmsOrderItemMapper.BaseResultMap"/>
    <collection property="historyList"
        columnPrefix="history_"
        resultMap="OmsOrderOperateHistoryMapper.BaseResultMap"/>
</resultMap>

<select id="getDetail" resultMap="orderDetailResultMap">
    SELECT o.*,
        oi.id item_id, oi.product_name item_product_name, oi.product_quantity item_product_quantity,
        oi.product_price item_product_price, oi.real_amount item_real_amount,
        ooh.id history_id, ooh.operate_man history_operate_man,
        ooh.create_time history_create_time, ooh.order_status history_order_status
    FROM oms_order o
    LEFT JOIN oms_order_item oi ON o.id = oi.order_id
    LEFT JOIN oms_order_operate_history ooh ON o.id = ooh.order_id
    WHERE o.id = #{id}
</select>
```

### 4.6 软删除设计

```java
// 删除订单 → 设置 deleteStatus = 1（软删除）
OmsOrder record = new OmsOrder();
record.setDeleteStatus(1);
OmsOrderExample example = new OmsOrderExample();
example.createCriteria().andIdIn(ids);
orderMapper.updateByExampleSelective(record, example);
```

### 4.7 OmsOrder 核心字段（40+ 字段）

| 字段分类 | 字段 | 说明 |
|---------|------|------|
| 基本信息 | orderSn, createTime, memberUsername | 订单号、创建时间、会员名 |
| 金额 | totalAmount, payAmount, freightAmount, promotionAmount, couponAmount, discountAmount | 总/应付/运费/促销/优惠券/折扣 |
| 支付 | payType, paymentTime | 支付方式、支付时间 |
| 物流 | deliveryCompany, deliverySn, deliveryTime | 快递公司、单号、发货时间 |
| 收货人 | receiverName, receiverPhone, receiverProvince, receiverCity, receiverRegion, receiverDetailAddress | 收货地址完整信息 |
| 发票 | billType, billHeader, billContent, billReceiverPhone, billReceiverEmail | 发票信息 |
| 状态 | status, confirmStatus, deleteStatus | 订单/确认/删除状态 |
| 积分 | integration, growth, useIntegration | 赠送积分/成长值、使用积分 |

---

## 五、核心设计亮点总结

1. **CASE WHEN 批量更新**：一条 SQL 完成多订单发货，性能远优于逐条更新
2. **操作审计链**：`oms_order_operate_history` 记录每次状态变更
3. **三表联查**：订单+商品明细+操作记录一次查询，columnPrefix 避免列名冲突
4. **软删除**：所有删除操作只改 `deleteStatus`，数据不丢失
5. **条件更新**：发货 SQL 的 `AND status = 1` 防止重复发货
6. **订单设置**：`oms_order_setting` 控制各类订单的超时时间

---

---

# 第二部分：Java 面试订单服务高频问题

## 面试题 1：CASE WHEN 批量更新的原理和优势？

**原理**：
```sql
UPDATE oms_order
SET delivery_sn = CASE id
    WHEN 100 THEN 'SF123456'
    WHEN 101 THEN 'YT789012'
    END,
    status = 2
WHERE id IN (100, 101) AND status = 1
```

等价于按 id 分支设定 `delivery_sn` 的值，不同订单设置不同的物流单号。

**优势**：
| 方式 | SQL 条数 | 网络往返 | 事务 |
|------|---------|---------|------|
| 逐条 UPDATE | N 条 | N 次 | 需要包裹事务 |
| CASE WHEN | 1 条 | 1 次 | 天然原子性 |

**面试回答建议**：
> MySQL 的 CASE WHEN 语法允许在一条 UPDATE 语句中为不同行设置不同的值。批量发货场景中，每个订单的物流单号和快递公司都不同，用 CASE WHEN 可以一条 SQL 搞定，比逐条更新效率高 N 倍，而且是原子操作。

---

## 面试题 2：软删除的设计考量和潜在问题？

**设计考量**：
- 数据恢复：误删可以改回来
- 审计合规：保留数据痕迹
- 关联查询：所有查询都要加 `delete_status = 0` 条件

**潜在问题**：
- 数据膨胀：已删除数据占用空间
- 唯一约束冲突：软删后原数据还在，新插入的同名数据会冲突
- 查询性能：每次都要过滤 `delete_status = 0`，需要加索引

**本项目的解决方案**：`deleteStatus = 0` 已在查询 SQL 的 WHERE 条件中固定。

---

## 面试题 3：订单详情的三表联查如何避免列名冲突？

使用 MyBatis 的 **columnPrefix**：

```xml
<collection property="orderItemList" columnPrefix="item_"
    resultMap="OmsOrderItemMapper.BaseResultMap"/>

<collection property="historyList" columnPrefix="history_"
    resultMap="OmsOrderOperateHistoryMapper.BaseResultMap"/>
```

SQL 中给子表的列加上前缀：
```sql
SELECT o.*,
    oi.id item_id,          -- order_item 的列加 item_ 前缀
    ooh.id history_id       -- operate_history 的列加 history_ 前缀
```

这样即使两个子表都有 `id`、`create_time` 等同名列，也不会冲突。

---

## 面试速查表

| 问题 | 一句话答案 |
|------|-----------|
| CASE WHEN | 一条 SQL 批量更新不同行不同值，原子高效 |
| 软删除 | deleteStatus 字段标记，查询必须过滤 |
| columnPrefix | 多表联查时给列加前缀，避免同名列冲突 |
| 操作历史 | 每次状态变更插入 oms_order_operate_history |
| 订单状态 | 0待付款→1待发货→2已发货→3已完成→4已关闭 |

---

*学习日期：2026-07-08*
