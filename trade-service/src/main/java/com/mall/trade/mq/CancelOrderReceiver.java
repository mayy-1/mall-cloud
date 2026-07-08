package com.mall.trade.mq;

import com.mall.trade.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 取消订单消息的处理者
 * Created by macro on 2018/9/14.
 */
@Component
@RabbitListener(queues = "mall.order.cancel")
@RequiredArgsConstructor
public class CancelOrderReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelOrderReceiver.class);

    /** 订单服务，用于处理取消订单的业逻辑 */
    private final IOrderService orderService;

    @RabbitHandler
    public void handle(Long orderId){
        orderService.cancelOrder(orderId);
        LOGGER.info("process orderId:{}", orderId);
    }
}
