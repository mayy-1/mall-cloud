package com.mall.marketing.mq;

import com.mall.marketing.domain.dto.SeckillHoldMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 秒杀占坑超时检查消费者
 * 占坑后 5 分钟未确认支付，触发库存回滚与资格释放
 */
@Component
@RabbitListener(queues = "mall.seckill.hold")
@RequiredArgsConstructor
public class SeckillHoldReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillHoldReceiver.class);

    private static final String SECKILL_STOCK_KEY = "seckill:stock:%d:%d";
    private static final String SECKILL_USERS_KEY = "seckill:users:%d:%d";
    private static final String SECKILL_HOLD_KEY = "seckill:hold:%d:%d:%d";

    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitHandler
    public void handle(SeckillHoldMessage message) {
        Long promotionId = message.getPromotionId();
        Long productId = message.getProductId();
        Long memberId = message.getMemberId();
        int quantity = message.getQuantity() != null ? message.getQuantity() : 1;

        String holdKey = String.format(SECKILL_HOLD_KEY, promotionId, productId, memberId);
        String stockKey = String.format(SECKILL_STOCK_KEY, promotionId, productId);
        String usersKey = String.format(SECKILL_USERS_KEY, promotionId, productId);

        // 占坑标记已不存在 → 用户已确认支付（或已回滚），跳过
        Boolean holdExists = redisTemplate.hasKey(holdKey);
        if (!Boolean.TRUE.equals(holdExists)) {
            LOGGER.info("占坑已确认或已释放，跳过, memberId={}, productId={}", memberId, productId);
            return;
        }

        // 回滚库存 + 移除用户资格 + 删除占坑标记
        redisTemplate.opsForValue().increment(stockKey, quantity);
        redisTemplate.opsForSet().remove(usersKey, memberId.toString());
        redisTemplate.delete(holdKey);
        LOGGER.info("占坑超时，已回滚库存并释放资格, promotionId={}, productId={}, memberId={}",
                promotionId, productId, memberId);
    }
}
