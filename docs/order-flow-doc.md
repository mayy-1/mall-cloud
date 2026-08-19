# 商城三种下单模式流程文档

> 项目：mall-cloud（微服务商城）
> 涉及服务：`order-service`（订单）、`cart-service`（购物车）、`marketing-service`（营销/秒杀）、`product-service`（商品/库存）、`member-service`（会员）

本文档梳理项目中三种下单模式的完整链路：

| 模式 | 入口 | 核心特性 |
|------|------|----------|
| **普通下单（buyNow）** | 商品详情页「立即购买」 | 单品、价格防篡改、同步下单 |
| **购物车下单** | 购物车勾选「去结算」 | 多 SKU、批量锁库存、优惠券/积分分摊 |
| **秒杀下单** | 秒杀专区「立即抢购」 | Redis Lua 原子扣减、MQ 异步落库、一人一单 |

---

## 一、普通下单（buyNow · 立即购买）

单商品，从商品详情页直接下单，跳过购物车。

### 1.1 流程总览

```
商品详情页「立即购买」
      │
      ▼
① 生成确认单  POST /order/buyNow/confirm
      │
      ▼
② 提交订单    POST /order/buyNow/create
      │
      ├── 支付成功  POST /order/paySuccess
      └── 超时取消  MQ 延时消息 → cancelOrder
```

### 1.2 阶段①：生成确认单

**接口**：`PortalOrderController.buyNowConfirm`
**实现**：`PortalOrderServiceImpl.buyNowConfirm`

```
┌─────────────────────────────────────────────────────────┐
│ ① 获取当前登录会员  memberClient.getCurrentMember()       │
│ ② 构建虚拟购物车记录  buildBuyNowItem(buyNowParam)        │
│ ③ [线程池并行] 收货地址 + 可用优惠券 + 积分规则            │
│ ④ 会员积分余额                                            │
│ ⑤ 计算金额合计  calcCartAmount                            │
└─────────────────────────────────────────────────────────┘
```

关键点：
- **不查库存、不做库存校验** —— 库存校验延后到提交订单阶段
- 三个互不依赖的 Feign 调用通过 `CompletableFuture` + 线程池 `orderConfirmExecutor` 并行执行，缩短响应耗时

返回 `ConfirmOrderResult`：

| 字段 | 说明 |
|------|------|
| `cartPromotionItemList` | 商品明细（1 条） |
| `memberReceiveAddressList` | 收货地址列表 |
| `couponHistoryDetailList` | 可用优惠券 |
| `calcAmount` | 金额（total/promotion/freight/pay） |
| `memberIntegration` | 会员积分余额 |
| `integrationConsumeSetting` | 积分抵扣规则 |

### 1.3 阶段②：提交订单

**接口**：`PortalOrderController.buyNow`（`@GlobalTransactional`）
**实现**：`PortalOrderServiceImpl.buyNow`

```
┌────────────────────────────────────────────────────────────┐
│ ① 获取当前登录会员                                          │
│ ② 价格防篡改 + 上下架校验  validateProduct(productId)        │
│ ③ 构建虚拟购物车记录，价格用后端查到的真实价覆盖              │
│ ④ 查真实可用库存  fetchRealStock = stock - lockStock         │
│ ⑤ 库存校验  hasStock(realStock >= quantity)                  │
│ ⑥ 优惠券处理（校验可用 + 金额分摊）                          │
│ ⑦ 积分抵扣（校验可用 + 抵扣金额）                            │
│ ⑧ 计算实付金额  handleRealAmount                             │
│ ⑨ 锁定库存  lockStock（lockStock + quantity）                │
│ ⑩ 构建订单 + 生成订单号                                      │
│ ⑪ 插入订单主表 + 明细                                        │
│ ⑫ 核销优惠券 + 扣减积分                                      │
│ ⑬ 发送延时消息：60 秒未支付自动取消                          │
└────────────────────────────────────────────────────────────┘
```

### 1.4 关键安全节点

| 节点 | 防护 | 说明 |
|------|------|------|
| `validateProduct` | 价格防篡改 | 丢弃前端传价，用 `product.getPrice()` |
| `validateProduct` | 上下架校验 | `publishStatus != 1` → 抛异常 |
| `fetchRealStock` | 库存防超卖 | `realStock = stock - lockStock` |
| `hasStock` | 库存拦截 | `realStock < quantity` → 抛「库存不足」 |
| `lockStock` | 下单锁仓 | `lockStock += quantity`，付款才真扣 |
| `@GlobalTransactional` | 分布式一致性 | 失败则订单/锁/券/积分全部回滚 |

### 1.5 阶段③：支付与超时

```
✅ 付款          POST /order/paySuccess
                    status → 1（待发货）
                    paySuccessDeductStock：stock-1、lockStock-1、sale+1

❌ 超时          MQ 延时消息 60 秒
                    CancelOrderReceiver → cancelOrder
                    status → 4（已关闭）
                    releaseStock：lockStock-1
```

---

## 二、购物车下单（多 SKU）

用户在购物车勾选多个商品一起结算。

### 2.1 流程总览

```
购物车页面勾选商品「去结算」
      │
      ▼
① 生成确认单  POST /order/generateConfirmOrder
      │
      ▼
② 提交订单    POST /order/generateOrder
      │
      ├── 支付成功
      └── 超时取消
```

### 2.2 阶段①：生成确认单

**接口**：`PortalOrderController.generateConfirmOrder`
**实现**：`PortalOrderServiceImpl.generateConfirmOrder`

```
┌─────────────────────────────────────────────────────────┐
│ ① 获取当前登录会员                                        │
│ ② [线程池并行] 购物车商品 + 收货地址 + 积分规则            │
│     cartClient.listCart(memberId, cartIds)               │
│ ③ 会员积分余额                                            │
│ ④ 查询可用优惠券  marketingCouponClient.listCart           │
│ ⑤ 计算金额合计  calcCartAmount                            │
└─────────────────────────────────────────────────────────┘
```

关键点：
- 购物车商品通过 `cartClient.listCart`（Feign）→ `cart-service` 的 `CartServiceImpl.listCart`，仅做字段拷贝，**不查库存**（与 buyNow 一致）
- 三个独立 Feign 调用同样走线程池并行

### 2.3 阶段②：提交订单

**接口**：`PortalOrderController.generateOrder`（`@GlobalTransactional`）
**实现**：`PortalOrderServiceImpl.generateOrder`

```
┌────────────────────────────────────────────────────────────┐
│ ① 校验收货地址                                              │
│ ② 获取会员 + 购物车商品（cartClient.listCart）               │
│ ③ 批量查库存  fetchStocksForCart（遍历 fetchRealStock）      │
│ ④ 拷贝商品 → 订单明细（OmsOrderItem）                       │
│ ⑤ 库存校验  hasStock（任意 SKU 不足整体拒绝）                │
│ ⑥ 优惠券处理（校验 + 按商品金额分摊）                        │
│ ⑦ 积分抵扣（校验 + 按商品金额比例分摊）                      │
│ ⑧ 计算实付金额  handleRealAmount                            │
│ ⑨ 锁定库存  lockStock（批量）                               │
│ ⑩ 构建订单主表（金额/收件人/订单号）                        │
│ ⑪ 插入订单主表 + 批量插入明细                               │
│ ⑫ 核销优惠券 + 扣减积分 + 删除购物车记录                    │
│ ⑬ 发送延时消息：超时未支付自动取消                          │
└────────────────────────────────────────────────────────────┘
```

### 2.4 与普通下单（buyNow）的差异

| 维度 | 普通下单 | 购物车下单 |
|------|---------|-----------|
| 商品数量 | 单 SKU | 多 SKU |
| 数据来源 | `buildBuyNowItem` 构造 | `cartClient.listCart` 查询 |
| 库存校验 | 单品 | 批量，任一不足整体拒绝 |
| 优惠券分摊 | 单品 | 按 `productPrice/total` 比例分摊 |
| 积分分摊 | 单品直接扣 | 按商品金额比例分摊 |
| 购物车清理 | 无 | `deleteCartItemList` 删除已下单记录 |

---

## 三、秒杀下单（高并发）

秒杀商品独立下单链路，Redis 冲在最前面挡流量，DB 只在后台慢慢消化。

### 3.1 流程总览

```
秒杀详情页「立即抢购」
      │
      ▼
① 执行秒杀  POST /seckill/execute（毫秒级同步返回）
      │  Lua 原子扣减 + 发 MQ
      ▼
② MQ 消费者异步创建订单（后台排队消化）
      │
      ├── 支付成功
      └── 超时取消（5 分钟）+ Redis 库存回滚
```

### 3.2 阶段①：执行秒杀（同步，<5ms）

**接口**：`SeckillController.executeSeckill`
**实现**：`SeckillServiceImpl.executeSeckill`

```
┌────────────────────────────────────────────────────────────┐
│ ① 获取当前登录会员  memberClient.getCurrentMember()          │
│ ② 校验秒杀活动状态  sms_flash_promotion.status == 1          │
│ ③ 校验秒杀商品关联  sms_flash_promotion_product_relation     │
│ ④ 执行 Lua 原子脚本（判断重复 + 判断库存 + 扣减）             │
│ ⑤ 成功 → 发 MQ 消息，立即返回「排队中」                      │
└────────────────────────────────────────────────────────────┘
```

### 3.3 Lua 原子脚本（核心）

```lua
-- KEYS[1]: seckill:stock:{promotionId}:{productId}  库存
-- KEYS[2]: seckill:users:{promotionId}:{productId}  已购用户 Set
-- ARGV[1]: memberId
-- ARGV[2]: limitPerUser

-- 1. 判断是否已购买（Set 去重，一人一单）
local purchased = redis.call('sismember', KEYS[2], ARGV[1])
if purchased == 1 then return -1 end

-- 2. 判断库存
local stock = tonumber(redis.call('get', KEYS[1]) or "0")
if stock <= 0 then return 0 end

-- 3. 原子扣减 + 记录
redis.call('decr', KEYS[1])
redis.call('sadd', KEYS[2], ARGV[1])
return 1
```

返回值：`1` 成功 | `0` 库存不足 | `-1` 重复购买

**为什么用 Lua**：判断重复、判断库存、扣减库存三步必须在 Redis 服务端原子完成，否则并发下会超卖。Lua 脚本整体执行，天然原子。

### 3.4 阶段②：MQ 消费者异步创建订单

**消费者**：`SeckillOrderReceiver.handle`
**实现**：`SeckillOrderServiceImpl.createSeckillOrder`（`@Transactional`）

```
┌────────────────────────────────────────────────────────────┐
│ ① Redisson 分布式锁兜底（防 MQ 重复消费）                   │
│ ② 获取会员信息  memberClient.getById                         │
│ ③ 查找商品 SKU                                              │
│ ④ 锁定 DB 库存  lockStock（lockStock + 1）                  │
│ ⑤ 构建订单（orderType=1 秒杀标识，payAmount=秒杀价）        │
│ ⑥ 插入订单主表                                              │
│ ⑦ 插入订单明细（productPrice=秒杀价）                       │
│ ⑧ 发送延时消息：5 分钟未支付自动取消                         │
└────────────────────────────────────────────────────────────┘
```

### 3.5 阶段③：支付与超时

```
✅ 付款          paySuccess
                    status → 1
                    paySuccessDeductStock

❌ 超时（5 分钟） cancelOrder
                    status → 4
                    releaseStock（释放 DB 锁库存）
                    rollbackSeckillStock（Redis 库存 +1，用户移出已购 Set，可再抢）
```

### 3.6 与普通/购物车下单的核心差异

| 维度 | 普通/购物车下单 | 秒杀下单 |
|------|---------------|---------|
| 库存扣减 | `hasStock` → `lockStock`（DB） | Lua 原子 `DECR`（Redis） |
| 用户限购 | 无 | Lua `SISMEMBER` + `SADD` 一人一单 |
| 价格来源 | `validateProduct` 查 DB 原价 | `flashPromotionPrice` 秒杀价 |
| 订单创建 | 同步，`@GlobalTransactional` | MQ 异步 + Redisson 锁兜底 |
| 优惠券/积分 | ✅ 参与 | ❌ 不参与 |
| 返回体验 | 等订单创建完才返回 | Lua 过 → 立即返回「排队中」 |
| 超时取消 | 60 秒（普通）/ 配置分钟（购物车） | 5 分钟 |

---

## 四、三种模式的共用机制

### 4.1 订单号生成（Redis 自增）

```java
// 格式：日期(8) + 来源(2) + 支付方式(2) + 自增序号(6)
// 例：20240818 01 00 000001
String key = REDIS_DATABASE + ":" + REDIS_KEY_ORDER_ID + date;
Long increment = redisService.incr(key, 1);
```

### 4.2 支付成功扣减库存（三模式共用）

```java
paySuccess(orderId, payType):
  订单 status → 1
  遍历明细 → skuStockClient.paySuccessDeductStock(skuId, quantity)
     stock-1、lockStock-1、sale+1
```

### 4.3 超时取消（MQ 延时消息 TTL + 死信）

下单时发送延时消息，消息在 TTL 队列等待过期后经死信交换机转发到取消队列，消费者执行取消。

```
sendMessage(orderId, delayTimes)
    → mall.order.direct.ttl 交换机
    → mall.order.cancel.ttl 队列（消息过期）
    → x-dead-letter-exchange: mall.order.direct
    → mall.order.cancel 队列
    → CancelOrderReceiver → cancelOrder
```

---

## 五、库存模型

`pms_sku_stock` 表两个关键字段：

| 字段 | 含义 |
|------|------|
| `stock` | 总库存（真实物理库存） |
| `lockStock` | 锁定库存（已下单未支付） |

```
可售库存 = stock - lockStock
```

生命周期：

```
下单瞬间:  stock=10  lockStock=0  →  lockStock+3  可售=7
付款成功:  stock=7   lockStock=0  →  可售=7  ✅
超时取消:  stock=10  lockStock=0  →  可售=10 🔄
```

---

## 六、涉及的接口一览

| 模式 | 接口 | 方法 | 服务 |
|------|------|------|------|
| 普通下单 | `/order/buyNow/confirm` | POST | order-service |
| 普通下单 | `/order/buyNow/create` | POST | order-service |
| 购物车下单 | `/order/generateConfirmOrder` | POST | order-service |
| 购物车下单 | `/order/generateOrder` | POST | order-service |
| 秒杀下单 | `/seckill/execute` | POST | marketing-service |
| 支付回调 | `/order/paySuccess` | POST | order-service |
| 购物车查询 | `/cart/list`（Feign `listCart`） | GET | cart-service |
| 库存锁定 | `/sku/{id}/stock/lock`（Feign） | POST | product-service |
| 库存释放 | `/sku/{id}/stock/release`（Feign） | POST | product-service |
| 支付扣减 | `/sku/{id}/stock/paySuccess`（Feign） | POST | product-service |
