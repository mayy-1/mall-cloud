package com.mall.trade.config;

import com.mall.trade.domain.SecKillQueueEnum;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 交易服务秒杀 RabbitMQ 配置
 * 声明秒杀订单队列、交换机及绑定关系（与 marketing-service 保持一致）
 */
@Configuration
public class SeckillRabbitMQConfig {

    /**
     * 秒杀订单 Direct 交换机
     */
    @Bean
    public DirectExchange seckillOrderDirect() {
        return new DirectExchange(SecKillQueueEnum.QUEUE_SECKILL_ORDER.getExchange());
    }

    /**
     * 秒杀订单队列（持久化）
     */
    @Bean
    public Queue seckillOrderQueue() {
        return new Queue(SecKillQueueEnum.QUEUE_SECKILL_ORDER.getName(), true);
    }

    /**
     * 绑定秒杀订单队列到交换机
     */
    @Bean
    public Binding seckillOrderBinding() {
        return BindingBuilder
                .bind(seckillOrderQueue())
                .to(seckillOrderDirect())
                .with(SecKillQueueEnum.QUEUE_SECKILL_ORDER.getRouteKey());
    }
}
