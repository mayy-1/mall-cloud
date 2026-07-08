# mall-cloud 交易服务学习笔记

---

# 第一部分：trade-service 模块完整梳理

## 一、模块定位

`trade-service` 是 mall-cloud 微服务电商平台的 **前台交易核心服务**，负责完整的下单流程：生成确认订单（预览）、提交订单（含库存锁定/优惠券核销/积分抵扣）、支付宝支付（PC+WAP）、支付成功回调处理、超时订单自动取消（RabbitMQ 延迟队列）、以及订单查询/取消/确认收货/退货申请等用户侧订单操作。

---

## 二、技术架构全景

```
              mall-gateway (:/mall-portal/**)
                         │
                         v
                 mall-auth (会员认证)
                         │
                         v
    ┌──────────────────────────────────────────────┐
    │            trade-service :8090               │
    │            (交易核心服务)                       │
    │                                              │
    │  ┌────────────────────────────────────────┐  │
    │  │ OrderController                   ★★   │  │
    │  │ POST /order/generateConfirmOrder      │  │  ← 确认订单页
    │  │ POST /order/generateOrder            │  │  ← ★ 提交订单
    │  │ POST /order/paySuccess               │  │  ← 支付回调
    │  └────────────────────────────────────────┘  │
    │  ┌────────────────────────────────────────┐  │
    │  │ AlipayController                 ★     │  │
    │  │ POST /alipay/pay  (PC网页支付)         │  │
    │  │ POST /alipay/webPay (WAP移动支付)      │  │
    │  │ POST /alipay/notify (异步通知)         │  │
    │  └────────────────────────────────────────┘  │
    │  ┌────────────────────────────────────────┐  │
    │  │ RabbitMQ 延迟队列                      │  │
    │  │ CancelOrderSender → TTL 消息           │  │
    │  │ CancelOrderReceiver → 超时取消         │  │
    │  └────────────────────────────────────────┘  │
    │                                              │
    │  Feign 调用:                                  │
    │  → cart-service:    获取/清空购物车           │
    │  → member-service:  会员/优惠券/收货地址      │
    │  → product-service: 扣减库存                 │
    │                                              │
    │  数据库: mall_order (MySQL)                  │
    │  缓存: Redis (订单号自增)                      │
    │  消息队: RabbitMQ (延迟取消)                   │
    │  支付: Alipay SDK (PC + WAP)                 │
    └──────────────────────────────────────────────┘
```

---

## 三、目录结构

```
trade-service/
├── pom.xml
└── src/main/
    ├── java/com/mall/trade/
    │   ├── TradeApplication.java          # 启动类
    │   ├── config/
    │   │   ├── AlipayConfig.java          # 支付宝配置
    │   │   ├── MyBatisConfig.java         # MyBatis 配置
    │   │   └── RabbitMqConfig.java        # RabbitMQ 配置
    │   ├── controller/
    │   │   ├── OrderController.java       # ★★ 订单控制器
    │   │   ├── AlipayController.java      # ★ 支付宝控制器
    │   │   └── ReturnApplyController.java # 退货申请（用户端）
    │   ├── domain/
    │   │   ├── dto/ (7个 DTO)
    │   │   └── QueueEnum.java             # RabbitMQ 队列枚举
    │   ├── feign/ (4个 Feign 接口)
    │   │   ├── OmsCartItemService.java    # → cart-service
    │   │   ├── UmsMemberService.java      # → member-service
    │   │   ├── UmsMemberCouponService.java # → member-service
    │   │   └── UmsMemberReceiveAddressService.java
    │   ├── mapper/ (10个 Mapper)
    │   ├── model/ (19个实体)
    │   ├── mq/
    │   │   ├── CancelOrderSender.java     # 延迟消息发送
    │   │   └── CancelOrderReceiver.java   # 延迟消息消费
    │   └── service/
    │       ├── IOrderService.java
    │       └── impl/
    │           └── OrderServiceImpl.java   ★★
    └── resources/
        ├── application.yml                # 含 RabbitMQ + Redis 配置
        ├── application-dev.yml
        └── mapper/ (10个 XML)
            └── PortalOrderMapperCustom.xml # 自定义订单 SQL
```

---

## 四、核心依赖解读（pom.xml）

| 依赖 | 作用 |
|------|------|
| `mybatis-spring-boot-starter` + `druid` + `mysql` | 数据库访问 |
| `pagehelper-spring-boot-starter` | 分页 |
| `spring-boot-starter-data-redis` | Redis（订单号自增） |
| `spring-boot-starter-amqp` | **RabbitMQ**（延迟取消订单） |
| `alipay-sdk-java` | **支付宝 SDK** |
| `spring-cloud-starter-alibaba-nacos-discovery/config` | Nacos |
| `knife4j-openapi3-jakarta-spring-boot-starter` | API 文档 |

---

## 五、核心功能详解

### 5.1 订单状态机

```
                        超时/手动取消
                   ┌─── ← ─────────────┐
                   v                    │
  0:待付款 → 1:待发货 → 2:已发货 → 3:已完成
   │ 支付成功    │ 发货      │ 确认收货
   │            │           │
   └──→ 更新状态, 扣库存     └──→ 交易完成
```

### 5.2 下单流程详解 ★★

```java
@Transactional
public Map<String, Object> generateOrder(OrderParam orderParam) {
    // ① 获取购物车中选中的商品（含促销信息）
    List<CartPromotionItem> cartPromotionItemList =
        cartItemService.listPromotion(memberId, orderParam.getCartIds());

    // ② 获取会员收货地址
    UmsMemberReceiveAddress address =
        addressService.getItem(orderParam.getMemberReceiveAddressId());

    // ③ 优惠券处理：遍历购物车商品，按优惠券类型计算
    //    全场券(useType=0) / 分类券(useType=1) / 商品券(useType=2)
    handleCoupon(cartPromotionItemList, orderParam.getCouponId());

    // ④ 积分抵扣：从积分使用规则获取抵扣比例
    //    例如：100积分 = 1元
    handleIntegration(cartPromotionItemList, orderParam.getUseIntegration());

    // ⑤ 库存校验与锁定：批量更新 SKU lock_stock
    //    UPDATE pms_sku_stock SET lock_stock = lock_stock + quantity
    lockStock(cartPromotionItemList);

    // ⑥ 订单号生成：Redis INCR + 格式拼接
    String orderSn = generateOrderSn(orderParam);

    // ⑦ 计算金额
    //    总金额 = sum(商品单价 × 数量)
    //    应付 = 总金额 - 促销 - 优惠券 - 积分 + 运费

    // ⑧ 批量插入订单和订单明细
    orderMapper.insert(order);
    orderItemMapper.insertList(orderItemList);

    // ⑨ 更新优惠券状态为已使用
    couponService.updateCouponStatus(orderParam.getCouponId());

    // ⑩ 扣减会员积分
    memberService.updateIntegration(memberId, -useIntegration);

    // ⑪ 清空购物车中已下单商品
    cartItemService.delete(orderParam.getCartIds());

    // ⑫ 发送 RabbitMQ 延迟消息（超时自动取消）
    cancelOrderSender.sendMessage(order.getId(), orderSetting.getNormalOrderOvertime());

    return result;
}
```

### 5.3 订单号生成 — Redis 分布式自增

```java
// 格式: yyyyMMdd + sourceType + payType + 6位递增
// 示例: 20260708 01 1 000001
//       ↑日期   ↑来源 ↑支付 ↑Redis INCR

String generateOrderSn(OrderParam orderParam) {
    String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
    // Redis INCR 获取 6 位递增序号
    Long orderId = redisService.incr("order_id", 1);
    String orderSn = dateStr + orderParam.getSourceType() + orderParam.getPayType()
        + String.format("%06d", orderId);
    return orderSn;
}
```

**为什么用 Redis INCR**：
- 原子性：INCR 是原子操作，保证分布式环境下的唯一性
- 性能：内存操作，毫秒级
- 简单：不需要用数据库自增或 Snowflake 算法

### 5.4 价格计算公式

```
totalAmount      = Σ(商品单价 × 购买数量)
promotionAmount  = Σ(各商品促销减免)
couponAmount     = 优惠券减免（按使用门槛判断）
integrationAmount = 积分抵扣金额（根据抵扣规则换算）
discountAmount   = 后台管理员手动折扣

应付金额 payAmount = totalAmount - promotionAmount
                     - couponAmount - integrationAmount
                     - discountAmount + freightAmount
```

### 5.5 支付成功处理

```java
@Transactional
public Integer paySuccess(Long orderId, Integer payType) {
    OmsOrder order = new OmsOrder();
    order.setId(orderId);
    order.setStatus(1);              // 0→1: 待付款→待发货
    order.setPaymentTime(new Date());
    order.setPayType(payType);

    // 更新SKU库存: stock -= quantity, lockStock -= quantity, sale += quantity
    orderMapper.updateSkuStock(orderId);

    return orderMapper.updateByPrimaryKeySelective(order);
}
```

### 5.6 RabbitMQ 延迟队列 — 超时取消

```
下单成功
    │
    v
CancelOrderSender.sendMessage(orderId, delayMinutes)
    │
    v
发送消息到 普通 Exchange → TTL Queue (设置消息过期时间)
    │
    │ 消息过期
    v
死信 Exchange → 死信 Queue (mall.order.cancel)
    │
    v
CancelOrderReceiver.handle(orderId)
    │
    ├─ 查询订单 status=0?
    │     ├─ YES → 设置 status=4 (已关闭)
    │     │       释放 SKU 锁定库存
    │     │       恢复优惠券状态
    │     │       恢复会员积分
    │     └─ NO  → 订单已支付，忽略
```

```java
// QueueEnum 定义
QUEUE_TTL_ORDER_CANCEL("mall.order.cancel.ttl",  // TTL 队列
    "mall.order.direct.ttl",                      // TTL Exchange
    "mall.order.cancel.ttl"),                     // Routing Key

QUEUE_ORDER_CANCEL("mall.order.cancel",           // 死信队列
    "mall.order.direct",                          // 死信 Exchange
    "mall.order.cancel");                         // Routing Key

// 发送延迟消息
public void sendMessage(Long orderId, long delayTimes) {
    rabbitTemplate.convertAndSend(
        QueueEnum.QUEUE_TTL_ORDER_CANCEL.getExchange(),
        QueueEnum.QUEUE_TTL_ORDER_CANCEL.getRouteKey(),
        orderId,
        message -> {
            message.getMessageProperties().setExpiration(String.valueOf(delayTimes));
            return message;
        }
    );
}
```

### 5.7 支付宝支付集成

```java
// PC网页支付
@PostMapping("/pay")
public CommonResult<String> pay(@RequestBody PayParam payParam) {
    AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
    request.setNotifyUrl(alipayConfig.getNotifyUrl());    // 异步通知地址
    request.setReturnUrl(alipayConfig.getReturnUrl());    // 同步跳转地址
    // 构造订单信息
    request.setBizContent(JSON.toJSONString(bizContent));
    // 调用支付宝SDK生成支付页面HTML
    AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
    return CommonResult.success(response.getBody());
}

// WAP移动支付
@PostMapping("/webPay")
public CommonResult<String> webPay(@RequestBody PayParam payParam) {
    AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
    request.setProductCode("QUICK_WAP_WAY");
    // ...同上
}
```

**支付宝异步通知验证**：

```java
@PostMapping("/notify")
public String notify(HttpServletRequest request) {
    // 1. 获取支付宝POST过来的参数
    Map<String, String> params = new HashMap<>();
    request.getParameterMap().forEach((key, values) -> params.put(key, values[0]));
    // 2. RSA2 签名验证
    boolean signVerified = AlipaySignature.rsaCheckV1(
        params, alipayConfig.getAlipayPublicKey(), "UTF-8", "RSA2");
    if (signVerified) {
        // 3. 验签通过，处理支付成功
        orderService.paySuccess(Long.parseLong(params.get("out_trade_no")), payType);
        return "success";
    }
    return "fail";
}
```

---

## 六、核心设计亮点总结

1. **原子下单**：11 步操作在 `@Transactional` 中完成，任一失败回滚
2. **Redis 订单号**：INCR 原子操作，高性能且保证分布式唯一
3. **延迟队列**：RabbitMQ TTL + 死信队列实现订单超时自动取消
4. **库存锁定**：下单时 lock_stock，支付后扣 real_stock，取消时释放 lock_stock
5. **多维度定价**：原价-促销-优惠券-积分-折扣-运费，层层递减
6. **支付宝双端**：PC 网页支付（FAST_INSTANT_TRADE_PAY）+ WAP 移动支付（QUICK_WAP_WAY）
7. **异步通知验签**：RSA2 公钥验证，防止伪造支付通知

---

---

# 第二部分：Java 面试交易服务高频问题

## 面试题 1：下单流程中如何保证数据一致性？

使用 Spring `@Transactional` 声明式事务：

```java
@Transactional(rollbackFor = Exception.class)
public Map<String, Object> generateOrder(OrderParam orderParam) {
    // 11 步操作在一个事务中
    // 任何一步抛出异常 → 全部回滚
}
```

**注意事项**：
- Feign 远程调用**不参与本地事务**（分布式事务问题）
- 库存锁定的 SQL 在事务中，回滚时会自动释放
- Redis 操作不参与事务（Redis 不支持回滚）

**面试回答建议**：
> 下单使用了 `@Transactional` 保证本地数据库操作的事务性。但需要注意两点：一是 Feign 远程调用不受本地事务控制，如果 Feign 调用成功后本地事务回滚，需要做补偿处理；二是 Redis 操作（订单号自增、积分扣减）不支持回滚。实际生产环境需要引入分布式事务方案如 Seata，或者用最终一致性的补偿机制。

---

## 面试题 2：RabbitMQ 延迟队列实现订单超时取消的原理？

```
TTL（Time To Live）+ 死信交换机（DLX）：

1. 消息发送到普通交换机，路由到 TTL 队列
2. TTL 队列没有消费者
3. 消息在 TTL 队列中存活指定时间后过期
4. 过期消息被投递到死信交换机
5. 死信交换机路由到真正的消费队列
6. 消费者监听该队列，执行取消逻辑
```

**面试回答建议**：
> RabbitMQ 本身没有直接的延迟队列功能，项目利用了 TTL（消息过期时间）+ DLX（死信交换机）的机制。消息先发到一个设置了 TTL 的队列，这个队列没有消费者。消息过期后自动被投递到绑定的死信交换机，再由死信交换机路由到真正的消费队列。消费者收到消息时，已经过了指定的延迟时间，此时执行订单取消逻辑。相比定时任务轮询数据库，延迟队列更精准且无数据库压力。

---

## 面试题 3：库存扣减的 `lock_stock` 设计是什么？

```
库存模型：
┌──────────────────────────────────────┐
│  stock = 100        (总库存)           │
│  lock_stock = 20    (已锁定库存)        │
│  可用库存 = stock - lock_stock = 80   │
└──────────────────────────────────────┘

下单时: lock_stock += quantity  → 锁定库存，不可超卖
支付后: stock -= quantity        → 扣减实际库存
       lock_stock -= quantity    → 释放锁定
       sale += quantity          → 销量增加
取消时: lock_stock -= quantity    → 仅释放锁定
```

**优点**：
- 防止超卖：锁定后其他用户无法购买
- 支持超时释放：取消订单只需释放 lock_stock
- 精确统计：stock（剩余）+ lock_stock（锁定）+ sale（已售）= 总量

---

## 面试题 4：支付宝异步通知为什么要验签？

**安全性**：防止伪造支付成功通知。

如果有人知道异步通知的 URL，发送一个伪造的 POST 请求说"支付成功"，不验签就会直接发货。

**验签流程**：
```java
// 1. 支付宝用商户公钥对通知参数签名
// 2. 商户收到通知后，用支付宝公钥验证签名
boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
// 3. 验签通过 → 信任此通知 → 更新订单状态
```

---

## 面试速查表

| 问题 | 一句话答案 |
|------|-----------|
| 下单事务 | @Transactional 包裹 11 步，任一失败回滚 |
| 延迟队列 | TTL 队列→消息过期→死信交换机→消费队列 |
| lock_stock | 下单锁定、支付扣减、取消释放，防超卖 |
| 订单号 | Redis INCR + yyyyMMdd + 来源 + 支付方式 |
| 支付宝验签 | RSA2 公钥验证，防伪造通知 |
| 价格计算 | 原价 - 促销 - 优惠券 - 积分 - 折扣 + 运费 |

---

*学习日期：2026-07-08*
