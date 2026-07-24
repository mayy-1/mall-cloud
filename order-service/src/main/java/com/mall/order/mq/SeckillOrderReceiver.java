package com.mall.order.mq;

import com.mall.order.domain.dto.SeckillOrderMessage;
import com.mall.order.service.ISeckillOrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 秒杀订单消息消费者
 * 从 RabbitMQ 消费秒杀订单消息，异步创建订单
 */
@Component
@RabbitListener(queues = "mall.seckill.order")
@RequiredArgsConstructor
public class SeckillOrderReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillOrderReceiver.class);
    private final ISeckillOrderService seckillOrderService;

    @RabbitHandler
    public void handle(SeckillOrderMessage message) {
        LOGGER.info("收到秒杀订单消息, memberId={}, productId={}, productName={}",
                message.getMemberId(), message.getProductId(), message.getProductName());
        try {
            seckillOrderService.createSeckillOrder(message);
            LOGGER.info("秒杀订单创建成功, memberId={}, productId={}", message.getMemberId(), message.getProductId());
        } catch (Exception e) {
            LOGGER.error("秒杀订单创建失败, memberId={}, productId={}", message.getMemberId(), message.getProductId(), e);
            // 生产环境应接入死信队列或重试机制
        }
    }
}

