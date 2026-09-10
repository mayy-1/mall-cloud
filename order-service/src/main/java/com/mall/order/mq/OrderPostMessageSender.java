package com.mall.order.mq;

import com.mall.order.domain.QueueEnum;
import com.mall.order.domain.dto.OrderPostMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

/**
 * 下单后处理消息发送者
 * 订单创建完成后，发送异步消息，交由消费者处理「标记优惠券/扣积分/清购物车/发延时取消消息」等副作用。
 */
@Component
@RequiredArgsConstructor
public class OrderPostMessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderPostMessageSender.class);

    private final AmqpTemplate amqpTemplate;

    public void send(OrderPostMessage message) {
        amqpTemplate.convertAndSend(
                QueueEnum.QUEUE_ORDER_POST.getExchange(),
                QueueEnum.QUEUE_ORDER_POST.getRouteKey(),
                message
        );
        LOGGER.info("send order post message, orderId={}", message.getOrderId());
    }
}
