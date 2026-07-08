# mall-cloud 营销服务学习笔记

---

# 第一部分：marketing-service 模块完整梳理

## 一、模块定位

`marketing-service` 是 mall-cloud 微服务电商平台的 **营销+内容+秒杀服务**，端口 **8084**。该模块将原单体项目中分散的 **SMS（营销系统）** 和 **CMS（内容管理系统）** 合并为一个微服务，统一管理优惠券、秒杀、首页推荐、专题等内容，数据库名为 `mall_marketing`。

**重要**：秒杀核心逻辑也在此模块中实现，包括 Redis Lua 原子脚本扣库存、RabbitMQ 异步削峰、定时预热/对账/自动上下线。

---

## 二、业务域全景

```
┌──────────────────────────────────────────────────────────────────┐
│                    marketing-service :8084                       │
│                                                                  │
│  ┌─────────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │  SMS 营销域           │  │  CMS 内容域      │  │  秒杀核心域    │ │
│  │                      │  │                 │  │              │ │
│  │  • 优惠券管理          │  │  • 商品专题      │  │  • Lua 原子扣 │ │
│  │  • 限时购/秒杀管理     │  │  • 优选区域      │  │  • MQ 异步下单│ │
│  │  • 首页轮播广告        │  │  • 帮助中心      │  │  • 预热/对账   │ │
│  │  • 首页品牌推荐        │  │  • 话题讨论      │  │  • 自动启停    │ │
│  │  • 首页新品推荐        │  │  • 举报管理      │  │  • 库存查询    │ │
│  │  • 首页人气推荐        │  │  • 专题评论      │  │              │ │
│  │  • 首页专题推荐        │  │                 │  │              │ │
│  └─────────────────────┘  └─────────────────┘  └──────────────┘ │
│                                                                  │
│  数据库: mall_marketing | 24+ 张表 | MyBatis Generator           │
│  Redis: 秒杀库存 / 已购 Set / 分布式锁 / 限流计数                  │
│  MQ:    RabbitMQ seckill.order.queue (异步削峰)                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 三、目录结构

```
marketing-service/
├── pom.xml
└── src/main/
    ├── java/com/mall/marketing/
    │   ├── MarketingApplication.java                 # 启动类 (@EnableScheduling)
    │   ├── config/
    │   │   ├── MyBatisConfig.java                    # MyBatis + 事务配置
    │   │   ├── RedissonConfig.java                   # Redisson 分布式锁
    │   │   └── SeckillRabbitMQConfig.java            # 秒杀队列/交换机/绑定
    │   ├── domain/
    │   │   └── QueueEnum.java                        # 队列枚举
    │   ├── controller/
    │   │   ├── FlashPromotionController.java         # 秒杀活动 CRUD
    │   │   ├── FlashPromotionSessionController.java  # 场次 CRUD
    │   │   ├── FlashPromotionProductRelationController.java # 商品关联 CRUD
    │   │   ├── CouponController.java                 # 优惠券 CRUD
    │   │   ├── CouponHistoryController.java          # 领取记录
    │   │   ├── HomeAdvertiseController.java          # 首页广告 CRUD
    │   │   ├── HomeBrandController.java              # 首页品牌推荐
    │   │   ├── HomeNewProductController.java         # 首页新品推荐
    │   │   ├── HomeRecommendProductController.java   # 首页人气推荐
    │   │   ├── HomeRecommendSubjectController.java   # 首页专题推荐
    │   │   ├── SubjectController.java                # 专题 CRUD
    │   │   ├── PrefrenceAreaController.java          # 优选区域 CRUD
    │   │   ├── PromotionController.java              # 促销计算 Feign 端点
    │   │   └── SeckillController.java                # 秒杀 API（前端调用）
    │   ├── service/
    │   │   ├── IFlashPromotionService.java
    │   │   ├── IFlashPromotionSessionService.java
    │   │   ├── IFlashPromotionProductRelationService.java
    │   │   ├── ICouponService.java
    │   │   ├── ICouponHistoryService.java
    │   │   ├── IHomeAdvertiseService.java
    │   │   ├── IHomeBrandService.java
    │   │   ├── IHomeNewProductService.java
    │   │   ├── IHomeRecommendProductService.java
    │   │   ├── IHomeRecommendSubjectService.java
    │   │   ├── ISubjectService.java
    │   │   ├── IPrefrenceAreaService.java
    │   │   ├── SeckillService.java                   # 秒杀服务接口
    │   │   └── impl/ (14个实现类)
    │   ├── task/
    │   │   ├── SeckillPreWarmTask.java               # 库存预热（每分钟）
    │   │   ├── SeckillStockCheckTask.java            # 库存对账（每5分钟）
    │   │   └── SeckillAutoStartStopTask.java         # 自动上下线（每分钟）
    │   ├── model/
    │   │   ├── SmsFlashPromotion.java                # 秒杀活动实体
    │   │   ├── SmsFlashPromotionSession.java         # 秒杀场次实体
    │   │   ├── SmsFlashPromotionProductRelation.java # 秒杀商品关联
    │   │   ├── SmsFlashPromotionLog.java
    │   │   ├── SmsCoupon.java                        # 优惠券实体
    │   │   ├── SmsCouponHistory.java
    │   │   ├── SmsCouponProductRelation.java
    │   │   ├── SmsCouponProductCategoryRelation.java
    │   │   ├── SmsHomeAdvertise.java                 # 首页广告
    │   │   ├── SmsHomeBrand.java
    │   │   ├── SmsHomeNewProduct.java
    │   │   ├── SmsHomeRecommendProduct.java
    │   │   ├── SmsHomeRecommendSubject.java
    │   │   ├── CmsSubject.java                       # 专题实体
    │   │   ├── CmsSubjectCategory.java
    │   │   ├── CmsSubjectComment.java
    │   │   ├── CmsSubjectProductRelation.java
    │   │   ├── CmsPrefrenceArea.java
    │   │   ├── CmsPrefrenceAreaProductRelation.java
    │   │   ├── CmsHelp.java
    │   │   ├── CmsHelpCategory.java
    │   │   ├── CmsMemberReport.java
    │   │   ├── CmsTopic.java
    │   │   ├── CmsTopicCategory.java
    │   │   ├── CmsTopicComment.java
    │   │   └── PmsProduct.java                       # 商品本地引用
    │   ├── dto/
    │   │   ├── SmsCouponParam.java                   # 优惠券参数 DTO
    │   │   ├── SmsFlashPromotionProduct.java         # 秒杀商品 DTO
    │   │   ├── SmsFlashPromotionSessionDetail.java   # 场次详情 DTO
    │   │   ├── SeckillOrderParam.java                # 秒杀下单请求
    │   │   ├── SeckillOrderMessage.java              # MQ 消息体
    │   │   └── SeckillProductDetailDTO.java          # 前台展示 DTO
    │   └── mapper/ (32 个 MyBatis Mapper 接口)
    └── resources/
        ├── application.yaml
        ├── application-dev.yaml
        ├── lua/seckill.lua                           # 秒杀 Lua 原子脚本
        └── mapper/ (32 个 MyBatis XML 映射文件)
```

---

## 四、核心依赖解读（pom.xml）

| 依赖 | 作用 |
|------|------|
| `mall-common` | 通用工具（CommonResult、CommonPage、RedisService） |
| `mall-api` | Feign 客户端接口 + 跨服务 DTO |
| `spring-boot-starter-web` | Web 控制器 |
| `mybatis-spring-boot-starter` | MyBatis ORM 框架 |
| `druid-spring-boot-3-starter` | Druid 连接池 |
| `mysql-connector-j` | MySQL 驱动 |
| `pagehelper-spring-boot-starter` | 分页插件 |
| `spring-cloud-starter-alibaba-nacos-discovery` | Nacos 服务发现 |
| `spring-cloud-starter-alibaba-nacos-config` | Nacos 配置中心 |
| `spring-boot-admin-starter-client` | Spring Boot Admin 监控客户端 |
| `knife4j-openapi3-jakarta-spring-boot-starter` | API 文档 |
| `spring-boot-starter-amqp` | RabbitMQ 消息队列 |
| `redisson-spring-boot-starter` | Redisson 分布式锁 |

---

## 五、数据模型设计（24 张核心表）

### 5.1 优惠券体系（4 张表）

```
sms_coupon (优惠券主表)
  ├── type:     0=全场赠券, 1=会员赠券, 2=购物赠券, 3=注册赠券
  ├── platform:  0=全部, 1=移动, 2=PC
  ├── useType:   0=全场通用, 1=指定分类, 2=指定商品
  ├── count:     发行数量
  ├── publishCount: 发行总量
  ├── receiveCount: 已领取数量
  ├── useCount:     已使用数量
  ├── amount:    优惠金额(元)
  ├── minPoint:  使用门槛金额
  ├── perLimit:  每人限领张数
  └── memberLevel: 可领取会员等级

  ├── sms_coupon_product_relation (优惠券-商品关联, useType=2)
  │     couponId → sms_coupon.id
  │     productId → pms_product.id

  ├── sms_coupon_product_category_relation (优惠券-分类关联, useType=1)
  │     couponId → sms_coupon.id
  │     productCategoryId → pms_product_category.id

  └── sms_coupon_history (优惠券领取/使用记录)
        couponId → sms_coupon.id
        memberId → ums_member.id
        useStatus: 0=未使用, 1=已使用, 2=已过期
        getType:   0=后台赠送, 1=主动领取
        orderId → oms_order.id
```

### 5.2 限时购/秒杀体系（4 + 1 张表）

```
sms_flash_promotion (限时购活动)
  ├── title:     活动名称
  ├── startDate: 活动开始日期
  ├── endDate:   活动结束日期
  └── status:    0=下线, 1=上线

sms_flash_promotion_session (限时购场次)
  ├── name:      场次名称（如"10:00场"）
  ├── startTime: 每日开始时间 (TIME 类型)
  ├── endTime:   每日结束时间 (TIME 类型)
  └── status:    0=不启用, 1=启用

sms_flash_promotion_product_relation (秒杀商品关联)
  ├── flashPromotionId → sms_flash_promotion.id
  ├── flashPromotionSessionId → session.id
  ├── productId → pms_product.id
  ├── flashPromotionPrice:   秒杀价
  ├── flashPromotionCount:   秒杀数量
  ├── flashPromotionLimit:   每人限购
  └── sort:                  排序

sms_flash_promotion_log (秒杀通知日志)
  └── 记录会员订阅秒杀通知

sms_seckill_order (秒杀订单记录，新增)
  ├── promotionId → sms_flash_promotion.id
  ├── productId → pms_product.id
  ├── memberId → ums_member.id
  ├── orderId → oms_order.id
  ├── seckillPrice:  实际秒杀价
  ├── quantity:       购买数量
  └── UNIQUE(memberId, productId, promotionId)  ← 数据库兜底唯一约束
```

### 5.3 首页推荐体系（5 张表）— 冗余字段设计

| 表 | 作用 | 冗余字段 |
|----|------|----------|
| `sms_home_advertise` | 首页轮播广告 | type(PC/APP)、status、sort、url |
| `sms_home_brand` | 首页品牌推荐 | brandId + brandName（冗余） |
| `sms_home_new_product` | 首页新品推荐 | productId + productName（冗余） |
| `sms_home_recommend_product` | 首页人气推荐 | productId + productName（冗余） |
| `sms_home_recommend_subject` | 首页专题推荐 | subjectId + subjectName（冗余） |

---

# 第二部分：秒杀完整实现详解

## 一、秒杀整体架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                          秒杀请求全链路                              │
│                                                                     │
│  H5/App                                                            │
│    │ POST /mall-portal/seckill/execute                              │
│    ▼                                                                │
│  ┌──────────────────┐                                               │
│  │ mall-gateway     │  SeckillRateLimitFilter                      │
│  │                  │  用户维度: 5次/秒   IP维度: 10次/秒           │
│  │                  │  超量 → HTTP 429                              │
│  └──────┬───────────┘                                               │
│         │ 放行                                                       │
│    ┌────▼──────────────────────────────┐                            │
│    │ marketing-service                 │                            │
│    │ SeckillController                 │                            │
│    │   ├─ 校验活动状态（DB查询）         │                            │
│    │   ├─ 校验商品关联（DB查询）         │                            │
│    │   ├─ 执行 Lua 原子脚本（Redis）    │ ← 核心：三步原子操作       │
│    │   └─ 发送 MQ 消息（RabbitMQ）     │                            │
│    └────┬──────────────────────────────┘                            │
│         │ RabbitMQ: mall.seckill.order.exchange                     │
│         │            ↓ mall.seckill.order.queue                     │
│    ┌────▼──────────────────────────────┐                            │
│    │ trade-service                     │                            │
│    │ SeckillOrderReceiver              │                            │
│    │   ├─ Redisson 分布式锁（兜底）     │                            │
│    │   ├─ 锁定 SKU 库存（lock_stock+1） │                            │
│    │   ├─ 创建 OmsOrder + OmsOrderItem │                            │
│    │   └─ 记录 sms_seckill_order       │                            │
│    └───────────────────────────────────┘                            │
│                                                                     │
│  定时任务:                                                           │
│    SeckillPreWarmTask       每分钟 → 预热库存到 Redis                │
│    SeckillStockCheckTask    每5分钟 → 对账 Redis vs DB              │
│    SeckillAutoStartStopTask 每分钟 → 自动上线/下线活动               │
│                                                                     │
│  取消订单回滚:                                                       │
│    OrderServiceImpl.cancelOrder() → rollbackSeckillStock()          │
│    → Redis INCR 恢复库存 + SREM 移除用户记录                         │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、第一步：网关限流（SeckillRateLimitFilter）

**位置**：`mall-gateway — SeckillRateLimitFilter.java`

秒杀流量入口第一道防线，在请求到达业务服务之前就拦截恶意/过度请求。

```java
@Component
public class SeckillRateLimitFilter implements GlobalFilter, Ordered {
    // 限流参数
    USER_RATE_LIMIT = 5;   // 每用户每秒 5 次
    IP_RATE_LIMIT = 10;    // 每IP每秒 10 次
    RATE_WINDOW_SECONDS = 1;

    // 仅拦截 /seckill/execute 路径
    // 先检查用户维度，再检查 IP 维度
    // 超限返回 HTTP 429 + JSON: {"code":429,"message":"请求过于频繁，请稍后再试"}
    // Redis 故障时降级放行（catch 异常返回 true）
}
```

**设计要点**：

| 要点 | 说明 |
|------|------|
| 双重限流 | userId + IP 两个维度独立计数，任一超限即拦截 |
| 固定窗口 | INCR key，首次设置1秒过期。简单高效，秒杀场景够用 |
| 降级策略 | Redis 故障时 `catch` 异常直接放行，不影响正常业务 |
| 仅限秒杀 | `path.contains("/seckill/execute")` 判断，不影响其他接口 |
| 执行顺序 | `getOrder() = -10`，鉴权过滤器之后执行，确保 userId 可用 |

---

## 三、第二步：Lua 原子脚本扣库存（核心）

**位置**：`resources/lua/seckill.lua`

```lua
-- KEYS[1]: seckill:stock:{promotionId}:{productId}  库存 Key
-- KEYS[2]: seckill:users:{promotionId}:{productId}  已购用户 Set
-- ARGV[1]: memberId  当前用户 ID
-- ARGV[2]: limitPerUser  每人限购数（暂用 Set 去重保证每人仅一次）
-- 返回: 1=成功, 0=库存不足, -1=重复购买

-- Step 1: 判断用户是否已购买（SISMEMBER 检查 Set）
local purchased = redis.call('sismember', KEYS[2], ARGV[1])
if purchased == 1 then
    return -1
end

-- Step 2: 判断库存是否充足（GET 检查）  
local stock = tonumber(redis.call('get', KEYS[1]) or "0")
if stock <= 0 then
    return 0
end

-- Step 3: 原子扣减（DECR 库存 + SADD 用户）
redis.call('decr', KEYS[1])
redis.call('sadd', KEYS[2], ARGV[1])

return 1
```

**为什么用 Lua？**

| 问题 | 无 Lua 方案 | Lua 方案 |
|------|-----------|---------|
| 超卖 | GET 后到 DECR 之间有并发窗口 | 整个判断+扣减在一条 Lua 中原子执行 |
| 重复买 | SETNX 和 DECR 是两个操作 | SISMEMBER + DECR 在一起，不存在间隙 |
| 网络开销 | 3-6 次 Redis 往返 | 1 次 EVAL 往返 |

**Redis Key 设计**：

| Key | 类型 | 含义 | 示例 |
|-----|------|------|------|
| `seckill:stock:{promotionId}:{productId}` | String (int) | 秒杀商品剩余库存 | `seckill:stock:1:100` → "50" |
| `seckill:users:{promotionId}:{productId}` | Set | 已购买用户 ID 集合 | `seckill:users:1:100` → {101, 102} |
| `seckill:lock:order:{memberId}:{productId}` | Redisson Lock | 分布式兜底锁 | 仅 trade-service 消费时使用 |
| `seckill:rate:user:{userId}` | String (int) | 用户限流计数器 | TTL 1 秒 |
| `seckill:rate:ip:{ip}` | String (int) | IP 限流计数器 | TTL 1 秒 |

---

## 四、第三步：营销服务核心逻辑（SeckillServiceImpl）

```java
@Service
public class SeckillServiceImpl implements SeckillService {

    public int executeSeckill(SeckillOrderParam param) {
        // 1. 校验秒杀活动状态（DB）
        SmsFlashPromotion promotion = flashPromotionMapper.selectByPrimaryKey(promotionId);
        if (promotion == null || promotion.getStatus() != 1) return STOCK_EMPTY;

        // 2. 校验秒杀商品关联（DB）
        // 防止秒杀不存在的关联关系

        // 3. 执行 Lua 原子脚本
        Long result = redisTemplate.execute(
            seckillScript,
            List.of(stockKey, usersKey),
            memberId.toString()
        );

        // 4. 根据返回值处理
        if (result == 1) {
            // 扣减成功 → 发送 MQ 异步订单消息
            rabbitTemplate.convertAndSend(
                QueueEnum.QUEUE_SECKILL_ORDER.getExchange(),
                QueueEnum.QUEUE_SECKILL_ORDER.getRouteKey(),
                message
            );
        }
        return result; // 1/0/-1
    }
}
```

**为什么 DB 校验不放在 Lua 里？**

- DB 校验（活动状态、商品关联）调用频率低、属于配置校验
- Lua 里只放高频、需要原子性的库存操作
- 分离后 Lua 脚本更简洁，Redis 执行更快
- DB 校验失败直接返回，不会进入 Lua 流程

---

## 五、第四步：RabbitMQ 异步削峰

**交换机/队列配置**（SeckillRabbitMQConfig）：

```java
// 交换机: mall.seckill.order.exchange
// 队列:   mall.seckill.order.queue
// 绑定:   seckill.order.# → mall.seckill.order.queue
```

**消息体**（SeckillOrderMessage）：

```java
@Data @Builder
public class SeckillOrderMessage {
    private Long promotionId;      // 秒杀活动ID
    private Long sessionId;        // 场次ID
    private Long productId;        // 商品ID
    private String productName;    // 商品名称
    private Long memberId;         // 会员ID
    private BigDecimal seckillPrice; // 秒杀价
    private Integer quantity;      // 数量
    private Date createTime;       // 创建时间
}
```

**为什么用 MQ 而不是同步创建订单？**

```
同步方案：
  用户请求 → Lua扣库存 → 创建订单(慢，涉及多个DB操作) → 返回结果
  问题：创建订单耗时长（200ms+），用户等待久，高并发打垮DB

MQ异步方案：
  用户请求 → Lua扣库存 → 发MQ(2ms) → 立即返回"秒杀成功，订单处理中"
                    ↓
              MQ消费者 → 创建订单（异步，不限速）
  优势：用户秒返回，订单创建和流量峰值解耦
```

---

## 六、第五步：MQ 消费者创建订单（SeckillOrderReceiver + SeckillOrderServiceImpl）

**位置**：`trade-service`

```java
// 消费者监听
@RabbitListener(queues = "mall.seckill.order.queue")
public class SeckillOrderReceiver {
    @RabbitHandler
    public void handle(SeckillOrderMessage message) {
        seckillOrderService.createSeckillOrder(message);
    }
}

// 订单创建（事务保护）
@Service
public class SeckillOrderServiceImpl implements ISeckillOrderService {

    @Transactional(rollbackFor = Exception.class)
    public Long createSeckillOrder(SeckillOrderMessage message) {
        // 1. Redisson 分布式锁兜底（最多等5秒，锁10秒）
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);

        // 2. 获取会员信息（Feign 调用 member-service）
        UmsMember member = memberService.getById(memberId);

        // 3. 查找商品 SKU 并锁定库存
        PmsSkuStock sku = skuStockMapper.selectByExample(...);
        sku.setLockStock(sku.getLockStock() + quantity);
        skuStockMapper.updateByPrimaryKeySelective(sku);

        // 4. 构建并插入 OmsOrder（orderType=1 秒杀订单）
        OmsOrder order = new OmsOrder();
        order.setMemberId(memberId);
        order.setOrderType(1);  // 秒杀订单
        order.setStatus(0);     // 待付款
        order.setOrderSn(generateOrderSn(order)); // Redis 自增生成
        orderMapper.insert(order);

        // 5. 插入 OmsOrderItem 订单商品项
        orderItemMapper.insert(orderItem);

        // 6. finally 释放锁
    }
}
```

### Redisson 分布式锁为什么是"兜底"？

```
正常路径:
  网关限流 → Lua SETNX用户去重 → Lua DECR库存
  → 用户去重已在 Redis 层完成，几乎不可能重复

兜底场景:
  极端情况：MQ 消息重复投递、Lua 脚本异常回滚导致两个请求都通过
  → Redisson tryLock(memberId + productId) 作为数据库层最后一道防线
  → 同一用户+商品只有一个线程能拿到锁
```

### 订单 cancellation 时回滚秒杀库存

```java
// OrderServiceImpl.cancelOrder()
@Override
public CommonResult cancelOrder(Long orderId) {
    // ... 取消订单正常逻辑 ...
    
    // 秒杀订单：回滚 Redis 库存
    if (order.getOrderType() == 1) {
        seckillOrderService.rollbackSeckillStock(promotionId, productId, memberId, 1);
    }
}

// SeckillOrderServiceImpl.rollbackSeckillStock()
public void rollbackSeckillStock(Long promotionId, Long productId, Long memberId, Integer quantity) {
    // 1. INCR 恢复库存
    redisService.incr("seckill:stock:" + promotionId + ":" + productId, quantity);
    // 2. SREM 移除用户记录，允许再次参与秒杀
    redisService.sRemove("seckill:users:" + promotionId + ":" + productId, memberId);
}
```

---

## 七、定时任务体系（3 个）

### 7.1 秒杀预热（每分钟）

```
SeckillPreWarmTask
  @Scheduled(cron = "0 * * * * ?") 每分钟
    → 查所有 status=1 且当日在有效期内的活动
    → 对每个活动调 warmUpStock(promotionId)
      → 遍历活动下所有商品关联
      → SETNX stockKey = flashPromotionCount (仅 key 不存在时设置)
      → 避免覆盖已运行的秒杀数据
```

**为什么需要预热？**
- 秒杀库存存储在 Redis，首次访问时 key 不存在
- 预热确保秒杀开始前库存已在 Redis 中，不会出现"秒杀开始但 Redis 无库存"的情况
- SETNX 语义保证不会覆盖已有数据

### 7.2 库存对账（每 5 分钟）

```
SeckillStockCheckTask
  @Scheduled(cron = "0 */5 * * * ?") 每5分钟
    → 查所有 status=1 且在有效期内的活动
    → 对每个活动调 reconcileStock(promotionId)
      → 遍历商品，对比 Redis stock vs DB flashPromotionCount
      → 不一致时以 DB 为准修正 Redis
```

**对账的意义**：Redis 是内存数据库，极端情况下（重启、网络分区、Lua 执行异常）可能丢数据。定期对账保证 Redis 库存和 DB 最终一致。

### 7.3 自动上下线（每分钟）

```
SeckillAutoStartStopTask
  @Scheduled(cron = "0 * * * * ?") 每分钟
    → 遍历所有秒杀活动
    → 到达 startDate 且未上线 → status=1 + 自动预热库存
    → 超过 endDate 且已上线 → status=0
```

运营后台创建秒杀活动后，不需要手动"上线"操作，到时间自动生效。

---

## 八、前台 API 接口

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/seckill/execute` | 需登录 | 执行秒杀下单 |
| GET | `/seckill/stock?promotionId=&productId=` | 公开 | 查询 Redis 剩余库存 |
| GET | `/seckill/list` | 公开 | 获取当前进行中的秒杀活动列表 |
| GET | `/seckill/detail?promotionId=&productId=` | 公开 | 获取单个秒杀商品详情 |

**返回值说明**：

| execute 返回值 | code | message |
|---------------|------|---------|
| 1 (成功) | 200 | "秒杀成功，订单处理中" |
| 0 (库存不足) | 500 | "库存不足，秒杀已结束" |
| -1 (重复) | 500 | "您已参与过该商品的秒杀，请勿重复购买" |

---

## 九、数据库兜底：唯一索引

```sql
-- 秒杀关联去重
ALTER TABLE sms_flash_promotion_product_relation
    ADD UNIQUE INDEX uk_promotion_product (flash_promotion_id, product_id);

-- 优惠券领券去重  
ALTER TABLE sms_coupon_history
    ADD UNIQUE INDEX uk_member_coupon (member_id, coupon_id);

-- 秒杀订单表（独立追踪）
CREATE TABLE sms_seckill_order (
    ...
    UNIQUE KEY uk_member_product_promotion (member_id, product_id, promotion_id)
);
```

**三重防重机制**：

| 层级 | 机制 | 拦截什么 |
|------|------|---------|
| Redis Lua | SISMEMBER 检查 Set | 同一用户短时间内重复请求 |
| Redisson | tryLock 分布式锁 | MQ 重复消费导致并发写 DB |
| MySQL | UNIQUE 唯一索引 | 极端情况下穿透 Redis 和锁的重复写入 |

---

## 十、原有功能：优惠券与首页推荐

### 10.1 优惠券核心逻辑

**创建优惠券**（先插主表，再插关联）：
```java
couponMapper.insert(coupon);    // 插入主表，获取自增 ID
if (useType == 2) relationMapper.insertList(products);  // 指定商品
if (useType == 1) categoryRelationMapper.insertList(cats); // 指定分类
```

**更新优惠券**（先删后插策略）：
```java
// 删除旧关联
relationMapper.deleteByExample(example);  // WHERE coupon_id = ?
// 插入新关联
relationMapper.insertList(newRelations);
```

### 10.2 首页推荐模板模式

四个首页推荐 Service 高度统一：
```
create(List)           → 循环插入
updateSort(id, sort)   → 只改排序
delete(ids)            → 批量删除
updateRecommendStatus  → 批量上下架
list(page, size)       → PageHelper 分页
```

### 10.3 PromotionController (stub)

```java
@RestController
@RequestMapping("/promotion")
public class PromotionController {
    // 供 cart-service Feign 调用，计算购物车促销
    // 当前为 stub 实现，直接返回输入
}
```

---

# 第三部分：Java 面试题（23 题）

## 秒杀专题（13 题）

### 面试题 1：Lua 脚本在秒杀中起什么作用？不用会怎样？

**项目实现**（seckill.lua）：

```lua
-- 三步原子操作
redis.call('sismember', usersKey, memberId)  -- 1. 检查是否已购
redis.call('get', stockKey)                   -- 2. 检查库存
redis.call('decr', stockKey)                  -- 3. 扣减库存
redis.call('sadd', usersKey, memberId)        --   记录用户
```

**不用 Lua 的问题**：

```
线程A: GET stock → 1 (有库存)
线程B: GET stock → 1 (有库存，还没被扣)
线程A: DECR stock → 0 ✓
线程B: DECR stock → -1 ✗ 超卖！
```

两个线程在 GET 和 DECR 之间有时间窗口，导致库存从 1 扣到 -1。

**Lua 的保证**：Redis 单线程执行 Lua，整个脚本执行期间不会被其他命令打断。3 步操作变成 1 步原子操作。

**面试回答**：
> Lua 脚本在 Redis 中原子执行，解决了秒杀场景下"判断-扣减"两步操作的并发竞争问题。如果不使用 Lua，`GET stock → if > 0 → DECR stock` 三步操作之间会有时间窗口，高并发下必然超卖。Lua 脚本将这三个操作合并为一个原子操作，Redis 单线程执行保证不会被中断。

---

### 面试题 2：秒杀系统为什么用 MQ？不用 MQ 会怎样？

**项目实现**：

```
秒杀成功 → 发 MQ (2ms) → 立即返回用户
                ↓
         MQ Consumer → 创建订单 (200ms)
```

**同步方案的问题**：

| 问题 | 说明 |
|------|------|
| 响应慢 | 创建订单涉及多表写入、Feign 调用，100-300ms |
| 并发崩溃 | 1000 QPS 秒杀全部打 DB，连接池耗尽 |
| 雪崩 | 订单服务慢 → 秒杀服务线程阻塞 → 整个链路挂 |

**MQ 的优势**：

| 优势 | 说明 |
|------|------|
| 削峰填谷 | 瞬时 1 万请求 → MQ 积压 → Consumer 按自己速度消费 |
| 解耦 | 秒杀服务只管扣库存发消息，不管订单怎么创建 |
| 用户秒返 | 从 300ms 降到 2ms，体验天壤之别 |

**面试回答**：
> MQ 在秒杀中的作用是削峰填谷和异步解耦。秒杀瞬间流量可能是平时的 100 倍以上，如果同步创建订单，数据库连接池会被瞬间打满导致雪崩。通过 MQ，秒杀服务扣完库存后只需发一条消息（2ms），订单创建由 Consumer 异步消费处理。这样用户秒返回"抢购成功"，订单系统按自己的处理能力匀速消费，实现了流量峰值和系统承载能力的解耦。

---

### 面试题 3：Redisson 分布式锁在秒杀中如何确保订单不重复？

**项目实现**：

```java
// trade-service 消费者中
String lockKey = "seckill:lock:order:" + memberId + ":" + productId;
RLock lock = redissonClient.getLock(lockKey);

// tryLock 参数: 等5秒, 锁10秒自动释放
if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
    // 创建订单...
} finally {
    if (lock.isHeldByCurrentThread()) lock.unlock();
}
```

**锁的粒度设计**：`memberId + productId`，确保同一用户+商品只有一个线程在创建订单。不影响其他用户或其他商品。

**Redisson 看门狗机制**：如果业务逻辑执行超过锁的过期时间（10秒），Redisson 的看门狗会**自动续期**，每 30 秒检查一次（默认），防止锁提前释放。

**面试回答**：
> 分布式锁在秒杀中作为 Redis 之后的第二道防线。虽然 Lua 脚本已经用 Set 做了用户去重，但极端情况下（MQ 重复投递、Lua 脚本执行异常），仍可能出现同一用户对同一商品的重复订单。此时 Redisson `tryLock(memberId+productId)` 保证只有一个线程能进入创建订单的逻辑。Redisson 基于 Redis 实现，支持自动续期（看门狗）和可重入，比简单的 SETNX 更可靠。

---

### 面试题 4：秒杀系统的防重机制有几层？

**三层防重**：

```
┌─────────────────────────────────────────┐
│  第1层: Redis Lua SISMEMBER             │
│  → 99.9% 的重复请求在此被拦截           │
│  → Set 集合去重，O(1) 查询              │
├─────────────────────────────────────────┤
│  第2层: Redisson 分布式锁               │
│  → MQ 消费时兜底，防止并发写 DB         │
│  → tryLock(memberId + productId)        │
├─────────────────────────────────────────┤
│  第3层: MySQL UNIQUE 索引                │
│  → uk_member_product_promotion           │
│  → 极端情况兜底，DB 层最终保障           │
└─────────────────────────────────────────┘
```

**面试回答**：
> 秒杀防重有三层防线。第一层是 Redis Lua 中的 SISMEMBER 检查，在内存中 O(1) 完成去重，拦截 99.9% 的重复请求。第二层是 Redisson 分布式锁，在 MQ 消费者中基于 memberId+productId 加锁，防止 MQ 重复消费导致并发写 DB。第三层是 MySQL UNIQUE 索引 `uk_member_product_promotion`，即使前两层都失效，数据库也会通过唯一约束拒绝重复插入。

---

### 面试题 5：网关限流是怎么做的？

**项目实现**（SeckillRateLimitFilter）：

```java
// 双重限流
USER_RATE_LIMIT = 5;   // 每用户每秒 5 次
IP_RATE_LIMIT = 10;    // 每 IP 每秒 10 次

// 固定窗口实现
if (redisService.get(key) >= limit) return false; // 限流
redisService.incr(key, 1);  // 计数+1
if (count == 1) redisService.expire(key, 1); // 首次设置过期
```

**为什么用固定窗口而非滑动窗口？**

- 秒杀请求密集且短暂（几秒内），固定窗口足够
- 实现简单：INCR + EXPIRE 两个命令
- 滑动窗口（ZSET + ZREMRANGEBYSCORE）命令多，高并发下性能差

**面试回答**：
> 网关层对秒杀接口做双重限流——按 userId 维度 5次/秒，按 IP 维度 10次/秒。实现方式是 Redis INCR + EXPIRE 固定窗口计数器。选择固定窗口而非滑动窗口的原因是秒杀流量集中在几秒内，固定窗口足以区分正常用户和脚本攻击。同时加了降级策略——Redis 故障时 catch 异常直接放行，不让限流成为单点故障。

---

### 面试题 6：秒杀为什么不直接用 MySQL 乐观锁？

**如果用乐观锁**：

```sql
UPDATE sms_flash_promotion_product_relation
SET flash_promotion_count = flash_promotion_count - 1
WHERE id = ? AND flash_promotion_count > 0;
-- 返回 affected_rows，0 表示库存不足
```

**为什么不选用？**

| 维度 | MySQL 乐观锁 | Redis Lua |
|------|------------|-----------|
| QPS | ~5000（行级锁竞争） | ~10万+（纯内存） |
| 延迟 | 1-5ms（磁盘IO + 网络） | <0.1ms |
| 竞争 | 行级锁 → 事务排队 | 单线程 → 无锁 |
| 回滚 | 事务回滚开销大 | 不需要回滚（原子操作） |

**面试回答**：
> MySQL 乐观锁可以做，但 QPS 上限在 5000 左右，而 Redis Lua 可以达到 10万+。秒杀场景特点是瞬时流量极高、持续时间极短，MySQL 的行锁和磁盘 IO 是瓶颈。Redis 纯内存操作 + 单线程模型，天然消除锁竞争，更适合秒杀。MySQL 在本项目中作为"兜底"角色——Redis 扛流量峰值，MySQL 通过 UNIQUE 索引保证最终一致性。

---

### 面试题 7：秒杀预热为什么要用 SETNX？

**预热代码**：

```java
Boolean stockExists = redisTemplate.hasKey(stockKey);
if (Boolean.FALSE.equals(stockExists)) {
    redisTemplate.opsForValue().set(stockKey, relation.getFlashPromotionCount());
}
```

**为什么不用简单的 SET？**

如果预热任务每分钟执行一次，用 `SET` 会覆盖秒杀进行中的实时库存数据。`hasKey` 检查 + 仅在不存在时 SET，确保"只有第一次预热才写入，后续秒杀中的库存数据不动"。

**注意**：这个 `hasKey + SET` 不是严格原子的（两个操作之间有间隙），但在预热场景下可以接受——预热在秒杀开始前执行，此时没有并发请求，不存在竞争。

**面试回答**：
> 预热使用 SETNX 语义（检查 key 不存在再写入），目的是：如果预热任务执行时秒杀已经开始（如重启后的补偿预热），不能覆盖 Redis 中已在运行的实时库存数据。预热的目标是"补上还没加载的库存"，而不是"重置库存"。

---

### 面试题 8：秒杀对账任务的作用是什么？

**对账逻辑**：

```java
// 每隔 5 分钟
for (商品关联) {
    int redisStock = redis.get("seckill:stock:" + promotionId + ":" + productId);
    int dbStock = relation.getFlashPromotionCount();
    if (redisStock != dbStock) {
        redis.set(stockKey, dbStock); // 以 DB 为准修正
    }
}
```

**不一致的可能原因**：

| 场景 | 说明 |
|------|------|
| Redis 重启 | 内存数据丢失，对账后从 DB 恢复 |
| Redis 主从延迟 | 主从切换时可能有少量数据丢失 |
| MQ 消费失败 | Redis 已扣但 DB 未扣，DB 回滚后 Redis 不知情 |

**面试回答**：
> 对账任务每5分钟比较 Redis 和 DB 的库存数据，发现不一致时以 DB 为准修正 Redis。这是分布式系统中常见的"最终一致性"保障手段。Redis 作为内存数据库，在重启、主从切换等场景下可能丢数据。对账机制确保即使这些极端情况发生，也能在下一轮对账时自动修正，避免了"Redis 显示有库存但 DB 实际已卖完"的尴尬。

---

### 面试题 9：秒杀请求的完整链路是怎样的？

```
1. 前端 POST /mall-portal/seckill/execute
     ↓
2. Gateway SeckillRateLimitFilter
     → 检查 userId 限流: seckill:rate:user:{userId} 是否 ≥ 5
     → 检查 IP 限流:   seckill:rate:ip:{ip} 是否 ≥ 10
     → 超限 → HTTP 429
     ↓ 放行
3. marketing-service SeckillController.executeSeckill()
     → 校验活动状态（DB: status=1, 日期范围内）
     → 校验商品关联（DB: promotion + product 存在）
     ↓
4. SeckillServiceImpl.executeSeckill()
     → Redis EVAL seckill.lua
       → SISMEMBER 检查用户是否已购买
       → GET 检查库存 > 0
       → DECR 库存 -1
       → SADD 记录用户
     → Lua 返回 1 (成功)
     ↓
5. RabbitMQ 发送消息
     → Exchange: mall.seckill.order.exchange
     → Queue: mall.seckill.order.queue
     ↓
6. trade-service SeckillOrderReceiver.handle()
     → Redisson tryLock(memberId + productId) 兜底
     → Feign 获取会员信息
     → 锁定 SKU 库存 (lock_stock + 1)
     → INSERT oms_order (orderType=1)
     → INSERT oms_order_item
     → 释放锁
```

**面试回答**：
> 秒杀全链路分为六步：网关限流 → 业务校验 → Lua 原子扣减 → MQ 异步消息 → 消费者分布式锁兜底 → 创建订单。网关卡流量，Lua 卡并发正确性，MQ 削峰，分布式锁和唯一索引兜底。每一层解决不同维度的问题，组合起来构成一个生产级秒杀系统。

---

### 面试题 10：秒杀库存回滚是如何实现的？

**触发时机**：用户在 trade-service 取消订单时。

```java
// OrderServiceImpl.cancelOrder()
if (order.getOrderType() == 1) {
    seckillOrderService.rollbackSeckillStock(promotionId, productId, memberId, 1);
}

// rollbackSeckillStock 做了两件事:
redisService.incr("seckill:stock:" + promotionId + ":" + productId, 1);    // 恢复库存
redisService.sRemove("seckill:users:" + promotionId + ":" + productId, memberId); // 移除购买记录
```

**面试回答**：
> 用户取消秒杀订单时，执行库存回滚：INCR 恢复 Redis 库存数量，SREM 从已购买用户 Set 中移除用户，允许用户再次参与该商品的秒杀。注意这只是在"订单未支付被取消"的情况下才回滚。如果订单已支付再退款，一般不复原秒杀库存。

---

### 面试题 11：秒杀活动自动上下线怎么实现的？

```java
@Scheduled(cron = "0 * * * * ?")  // 每分钟
void autoStartStop() {
    for (promotion : allPromotions) {
        if (now > startDate && now < endDate && status != 1) {
            promotion.setStatus(1);     // 自动上线
            warmUpStock(promotionId);   // 自动预热库存
        }
        if (now > endDate && status != 0) {
            promotion.setStatus(0);     // 自动下线
        }
    }
}
```

**面试回答**：
> 通过 `@Scheduled` 定时任务每分钟扫描所有秒杀活动，根据当前时间与活动的 startDate/endDate 比较，自动将活动状态改为上线或下线。上线时同时触发库存预热，将 DB 的 flashPromotionCount 加载到 Redis。这样运营只需配置活动日期，系统自动管理活动的生命周期。

---

### 面试题 12：秒杀系统如果 Redis 挂了怎么办？

**项目中的应对**：

| 层级 | 策略 |
|------|------|
| 网关限流 | `catch` 异常后放行，不让限流成为单点 |
| Lua 扣减 | 失败直接返回失败，不尝试降级到 DB |
| 库存对账 | 定时任务 Redis 恢复后自动从 DB 补数据 |

**面试回答**：
> 秒杀场景下 Redis 是关键依赖，挂了就是挂了，不应尝试降级到 MySQL——DB 扛不住秒杀流量。正确的做法是：限流层降级放行（避免误拦截），Lua 层快速失败返回用户"系统繁忙"，运维层面做 Redis 哨兵/集群保证高可用。Redis 恢复后，对账任务会自动从 DB 同步库存数据。

---

### 面试题 13：如何设计能支撑 10 万 QPS 的秒杀系统？

**分层架构**：

```
┌──────────────────────────────────────────────┐
│  CDN / 静态化  →  商品详情页静态化，不走后端  │
├──────────────────────────────────────────────┤
│  Nginx 限流   →  单机 1万 QPS，集群横向扩展   │
├──────────────────────────────────────────────┤
│  网关限流     →  用户维度 + IP 维度           │
├──────────────────────────────────────────────┤
│  业务服务      →  Lua 扣库存（Redis Cluster）  │
│                →  MQ 异步订单（RocketMQ/Kafka）│
├──────────────────────────────────────────────┤
│  订单服务      →  分布式锁兜底（Redisson）     │
│                →  数据库分库分表               │
├──────────────────────────────────────────────┤
│  对账补偿      →  定时任务 Redis vs DB 对账    │
└──────────────────────────────────────────────┘
```

核心公式：
> **能处理的 QPS = Redis 单机 QPS × 分片数**（Redis Cluster 可达 50万+ QPS）
>
> **库存扣减在 Redis，瓶颈不在 DB**

本项目已经实现了其中的 Lua + MQ + 限流 + 分布式锁 + 对账，距离 10万 QPS 的主要差距是 Redis Cluster 和 DB 分库分表。

---

## 基础专题（10 题）

### 面试题 14：优惠券的 useType 三级匹配是怎样设计的？

useType 分三种：0=全场通用（订单金额 ≥ minPoint）、1=指定分类（查 `coupon_product_category_relation`）、2=指定商品（查 `coupon_product_relation`）。通过独立关联表存储而非逗号分隔 ID，可利用数据库索引高效查询。

---

### 面试题 15：优惠券的"先删后插"策略有什么优缺点？

更新优惠券时，先 `deleteRelationByCouponId` 删除所有旧关联，再 `insertRelationBatch` 批量插入新关联。优点：实现简单，最终状态=新列表，不需要 diff 计算。缺点：主键 ID 不连续，无法保留历史变更记录。在关联数据量不大的场景下是最实用的选择。

---

### 面试题 16：为什么首页推荐表要冗余存储名称？

冗余存储 brandName/productName 避免每次展示首页都 JOIN 或跨服务 Feign 查询。首页是最高频访问页面，一次 JOIN 省下的性能非常可观。代价是源数据变更时需同步更新冗余字段。

---

### 面试题 17：MyBatis Generator 生成的代码有什么特点？

MBG 生成实体类 + Example 条件构造器 + Mapper 接口 + XML 映射文件，提供 12 个标准 CRUD 方法。自定义扩展通过 `XxxMapperCustom extends XxxMapper` + 自定义 XML 的 `<resultMap extends="BaseResultMap">` 实现。

---

### 面试题 18：BLOB 字段为什么要在 Mapper 中单独分离查询？

CMS 模块的专题内容、话题内容等使用 BLOB 存储富文本。MBG 提供 `selectByExample`（不含 BLOB，列表快）和 `selectByExampleWithBLOBs`（含 BLOB，详情用）两种方法。如果列表也加载 BLOB，每行可能多出几十 KB，严重影响 IO 和网络传输效率。

---

### 面试题 19：Example 动态查询条件是如何工作的？

Example 是 MBG 生成的类型安全查询构造器。`createCriteria()` 内的条件用 AND 连接，多个 `or()` 之间用 OR 连接。内部通过 `oredCriteria` 列表存储条件组，最终动态拼接 WHERE 子句。

---

### 面试题 20：秒杀场次为什么用 TIME 类型而非 DATETIME？

场次是每天重复的固定时间段（如每天 10:00-12:00），活动日期由 `sms_flash_promotion.startDate/endDate` 控制。TIME 类型可以直接做 `LocalTime.now().isAfter(startTime)` 比较，避免了 DATETIME 日期部分的冗余。

---

### 面试题 21：为什么 SMS 和 CMS 合并为一个服务？

两个子域业务关联性强（首页推荐同时用两者的数据），共用同一个数据库，每个子域表都不多。拆成两个服务会增加运维成本和 Feign 调用开销，违背"高内聚"原则。

---

### 面试题 22：insertSelective 和 insert 有什么区别？

`insert` 全量插入所有字段，null 会覆盖数据库默认值；`insertSelective` 只插入非 null 字段，保留数据库默认值（如 create_time = NOW()）。新增操作优先用 insertSelective。

---

### 面试题 23：分页查询如何统一处理？

使用 PageHelper 插件：`PageHelper.startPage(pageNum, pageSize)` → 通过 ThreadLocal 传递分页参数 → MyBatis 拦截器在 SQL 后追加 LIMIT → 查询 COUNT 获取总数 → `CommonPage.restPage(list)` 转换为统一分页对象。

---

## 附：面试速查表

### 秒杀专题

| 问题 | 核心点 |
|------|--------|
| Lua 作用 | 原子化"判断+扣减"，消除并发窗口 |
| MQ 作用 | 削峰填谷，秒杀服务和订单服务异步解耦 |
| 防重机制 | Lua SISMEMBER → Redisson 分布式锁 → MySQL UNIQUE |
| 限流方案 | 网关 Redis 固定窗口，userId/IP 双重维度 |
| 为什么不用乐观锁 | MySQL ~5000 QPS，Redis 内存 10万+ QPS |
| 预热为什么 SETNX | 避免覆盖秒杀进行中的实时库存 |
| 库存回滚 | 取消秒杀订单 → INCR + SREM 恢复 |
| 10万 QPS | CDN + Nginx + Redis Cluster + MQ + 分库分表 |

### 基础专题

| 问题 | 核心点 |
|------|--------|
| 优惠券 useType | 全场/分类/商品 三级，关联表存储 |
| 先删后插 | 简单可靠，缺点主键不连续 |
| 冗余字段 | 空间换时间，避免 JOIN |
| MBG 扩展 | Custom extends Mapper + extends BaseResultMap |
| BLOB 分离 | 列表不含 BLOB，详情才加载 |
| TIME 类型 | 每天重复时间段，非一次性时间点 |
| SMS+CMS 合并 | 业务关联强、粒度适中 |
| insertSelective | 非 null 字段写入，保留默认值 |
| PageHelper | 拦截器自动追加 LIMIT + COUNT |

---

*学习日期：2026-07-08 | 更新：加入秒杀完整实现（Lua+MQ+Redisson+限流+预热+对账+回滚+自动启停）*
