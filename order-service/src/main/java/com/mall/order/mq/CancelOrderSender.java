package com.mall.order.mq;

import com.mall.order.domain.QueueEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 取消订单消息的发出者
 * Created by macro on 2018/9/14.
 */
@Component
@RequiredArgsConstructor
public class CancelOrderSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelOrderSender.class);

    /** AMQP消息模板，用于发送消息到RabbitMQ */
    private final AmqpTemplate amqpTemplate;

    public void sendMessage(Long orderId, final long delayTimes){
        amqpTemplate.convertAndSend(QueueEnum.QUEUE_TTL_ORDER_CANCEL.getExchange(), QueueEnum.QUEUE_TTL_ORDER_CANCEL.getRouteKey(), orderId, message -> {
            message.getMessageProperties().setExpiration(String.valueOf(delayTimes));
            return message;
        });
        LOGGER.info("send orderId:{}", orderId);
    }
}

