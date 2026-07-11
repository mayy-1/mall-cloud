package com.mall.trade.service.impl;

import com.mall.api.client.MemberClient;
import com.mall.api.dto.MemberDTO;
import com.mall.trade.domain.dto.SeckillOrderMessage;
import com.mall.trade.mapper.OmsOrderItemMapper;
import com.mall.trade.mapper.OmsOrderMapper;
import com.mall.trade.mapper.PmsSkuStockMapper;
import com.mall.trade.model.OmsOrder;
import com.mall.trade.model.OmsOrderItem;
import com.mall.trade.model.PmsSkuStock;
import com.mall.trade.service.ISeckillOrderService;
import com.mym.mall.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单服务实现
 * 核心流程：Redisson 兜底锁 → 锁定 SKU 库存 → 创建订单 → 扣减秒杀库存
 */
@Service
@RequiredArgsConstructor
public class SeckillOrderServiceImpl implements ISeckillOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillOrderServiceImpl.class);

    private static final String SECKILL_LOCK_KEY = "seckill:lock:order:%d:%d";

    private final RedissonClient redissonClient;
    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemMapper orderItemMapper;
    private final PmsSkuStockMapper skuStockMapper;
    private final MemberClient memberClient;
    private final RedisService redisService;

    @Value("${redis.key.orderId}")
    private String REDIS_KEY_ORDER_ID;
    @Value("${redis.database}")
    private String REDIS_DATABASE;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSeckillOrder(SeckillOrderMessage message) {
        Long memberId = message.getMemberId();
        Long productId = message.getProductId();
        String lockKey = String.format(SECKILL_LOCK_KEY, memberId, productId);

        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 1. Redisson 分布式锁兜底（最多等待 5 秒，锁定 10 秒自动释放）
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!acquired) {
                LOGGER.warn("获取分布式锁失败, memberId={}, productId={}", memberId, productId);
                throw new RuntimeException("系统繁忙，请稍后重试");
            }

            // 2. 获取会员信息
            MemberDTO member = memberClient.getById(memberId).getData();

            // 3. 查找商品 SKU（取第一个 SKU）
            PmsSkuStock skuExample = new PmsSkuStock();
            skuExample.setProductId(productId);
            List<PmsSkuStock> skuList = skuStockMapper.selectByCondition(skuExample);
            if (skuList.isEmpty()) {
                throw new RuntimeException("商品SKU不存在, productId=" + productId);
            }
            PmsSkuStock skuStock = skuList.get(0);

            // 4. 锁定库存（lock_stock + 1）
            skuStock.setLockStock((skuStock.getLockStock() == null ? 0 : skuStock.getLockStock()) + message.getQuantity());
            skuStockMapper.updateByPrimaryKeySelective(skuStock);

            // 5. 构建订单对象
            OmsOrder order = new OmsOrder();
            order.setMemberId(memberId);
            order.setMemberUsername(member != null ? member.getUsername() : "");
            order.setTotalAmount(message.getSeckillPrice().multiply(new BigDecimal(message.getQuantity())));
            order.setPayAmount(message.getSeckillPrice().multiply(new BigDecimal(message.getQuantity())));
            order.setFreightAmount(BigDecimal.ZERO);
            order.setPromotionAmount(BigDecimal.ZERO);
            order.setIntegrationAmount(BigDecimal.ZERO);
            order.setCouponAmount(BigDecimal.ZERO);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setPayType(0);          // 待支付
            order.setSourceType(1);       // App
            order.setStatus(0);           // 待付款
            order.setOrderType(1);        // 秒杀订单
            order.setCreateTime(message.getCreateTime() != null ? message.getCreateTime() : new Date());
            order.setConfirmStatus(0);
            order.setDeleteStatus(0);
            order.setIntegration(0);
            order.setGrowth(0);
            order.setPromotionInfo("秒杀活动ID:" + message.getPromotionId());
            order.setOrderSn(generateOrderSn(order));

            // 6. 插入订单
            orderMapper.insert(order);

            // 7. 构建订单商品项
            OmsOrderItem orderItem = new OmsOrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderSn(order.getOrderSn());
            orderItem.setProductId(productId);
            orderItem.setProductName(message.getProductName());
            orderItem.setProductPrice(message.getSeckillPrice());
            orderItem.setProductQuantity(message.getQuantity());
            orderItem.setProductSkuId(skuStock.getId());
            orderItem.setProductSkuCode(skuStock.getSkuCode());
            orderItem.setPromotionName("秒杀价");
            orderItem.setPromotionAmount(BigDecimal.ZERO);
            orderItem.setCouponAmount(BigDecimal.ZERO);
            orderItem.setIntegrationAmount(BigDecimal.ZERO);
            orderItem.setRealAmount(message.getSeckillPrice());
            orderItem.setGiftIntegration(0);
            orderItem.setGiftGrowth(0);
            orderItemMapper.insert(orderItem);

            LOGGER.info("秒杀订单创建成功, orderId={}, orderSn={}", order.getId(), order.getOrderSn());
            return order.getId();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取分布式锁被中断", e);
        } finally {
            // 释放锁（仅当当前线程持有锁时释放）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 生成订单编号
     * 格式：日期 + 来源 + 支付方式 + 自增序号
     */
    private String generateOrderSn(OmsOrder order) {
        StringBuilder sb = new StringBuilder();
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ORDER_ID + date;
        Long increment = redisService.incr(key, 1);
        sb.append(date);
        sb.append(String.format("%02d", order.getSourceType()));
        sb.append(String.format("%02d", order.getPayType()));
        String incrementStr = increment.toString();
        if (incrementStr.length() <= 6) {
            sb.append(String.format("%06d", increment));
        } else {
            sb.append(incrementStr);
        }
        return sb.toString();
    }

    @Override
    public void rollbackSeckillStock(Long promotionId, Long productId, Long memberId, Integer quantity) {
        String stockKey = String.format("seckill:stock:%d:%d", promotionId, productId);
        String usersKey = String.format("seckill:users:%d:%d", promotionId, productId);

        // 1. 恢复 Redis 库存
        redisService.incr(stockKey, quantity);
        LOGGER.info("秒杀库存已回滚, promotionId={}, productId={}, quantity={}", promotionId, productId, quantity);

        // 2. 从已购买用户集合中移除该用户（允许再次参与秒杀）
        redisService.sRemove(usersKey, memberId);
        LOGGER.info("已移除用户购买记录, memberId={}, 用户可再次参与秒杀", memberId);
    }
}
