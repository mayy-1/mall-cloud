package com.mall.order.config;

import com.mall.order.domain.QueueEnum;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 下单后处理 RabbitMQ 配置
 * 用于异步处理下单后的副作用：标记优惠券已使用、扣减会员积分、清理购物车、发送延时取消订单消息。
 */
@Configuration
public class OrderPostRabbitMQConfig {

    /** 下单后处理 Direct 交换机 */
    @Bean
    public DirectExchange orderPostDirect() {
        return new DirectExchange(QueueEnum.QUEUE_ORDER_POST.getExchange());
    }

    /** 下单后处理队列（持久化） */
    @Bean
    public Queue orderPostQueue() {
        return new Queue(QueueEnum.QUEUE_ORDER_POST.getName(), true);
    }

    /** 绑定下单后处理队列到交换机 */
    @Bean
    public Binding orderPostBinding() {
        return BindingBuilder
                .bind(orderPostQueue())
                .to(orderPostDirect())
                .with(QueueEnum.QUEUE_ORDER_POST.getRouteKey());
    }
}
