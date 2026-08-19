package com.mall.order.config;

import com.mall.order.domain.QueueEnum;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 超时取消订单 RabbitMQ 配置（TTL + DLX 延时消息模式）
 */
@Configuration
public class CancelOrderRabbitMQConfig {

    /** 主交换机：接收 TTL 队列转发的过期消息 */
    @Bean
    public DirectExchange orderDirect() {
        return new DirectExchange(QueueEnum.QUEUE_ORDER_CANCEL.getExchange());
    }

    /** 主队列：消费者监听 */
    @Bean
    public Queue orderCancelQueue() {
        return new Queue(QueueEnum.QUEUE_ORDER_CANCEL.getName(), true);
    }

    /** 主队列绑定到主交换机 */
    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder
                .bind(orderCancelQueue())
                .to(orderDirect())
                .with(QueueEnum.QUEUE_ORDER_CANCEL.getRouteKey());
    }

    /** TTL 交换机：接收延时消息 */
    @Bean
    public DirectExchange orderDirectTtl() {
        return new DirectExchange(QueueEnum.QUEUE_TTL_ORDER_CANCEL.getExchange());
    }

    /** TTL 队列：消息在此等待过期，过期后自动转发到主交换机 */
    @Bean
    public Queue orderCancelTtlQueue() {
        Map<String, Object> args = new HashMap<>();
        // 过期后转发到主交换机
        args.put("x-dead-letter-exchange", QueueEnum.QUEUE_ORDER_CANCEL.getExchange());
        // 过期后路由键指向主队列
        args.put("x-dead-letter-routing-key", QueueEnum.QUEUE_ORDER_CANCEL.getRouteKey());
        return QueueBuilder.durable(QueueEnum.QUEUE_TTL_ORDER_CANCEL.getName())
                .withArguments(args)
                .build();
    }

    /** TTL 队列绑定到 TTL 交换机 */
    @Bean
    public Binding orderCancelTtlBinding() {
        return BindingBuilder
                .bind(orderCancelTtlQueue())
                .to(orderDirectTtl())
                .with(QueueEnum.QUEUE_TTL_ORDER_CANCEL.getRouteKey());
    }
}
