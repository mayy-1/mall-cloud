package com.mall.order.mq;

import com.mall.order.domain.dto.OrderPostMessage;
import com.mall.order.service.IPortalOrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 下单后处理消息消费者
 * 异步处理下单后的副作用：标记优惠券已使用、扣减会员积分、清理购物车、发送延时取消订单消息。
 */
@Component
@RabbitListener(queues = "mall.order.post")
@RequiredArgsConstructor
public class OrderPostMessageReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderPostMessageReceiver.class);

    private final IPortalOrderService portalOrderService;

    @RabbitHandler
    public void handle(OrderPostMessage message) {
        LOGGER.info("收到下单后处理消息, orderId={}, memberId={}", message.getOrderId(), message.getMemberId());
        try {
            portalOrderService.handlePostOrder(message);
            LOGGER.info("下单后处理完成, orderId={}", message.getOrderId());
        } catch (Exception e) {
            LOGGER.error("下单后处理失败, orderId={}", message.getOrderId(), e);
            // 生产环境应接入死信队列或重试机制
        }
    }
}
