package com.mall.marketing.config;

import com.mall.marketing.domain.QueueEnum;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 秒杀 RabbitMQ 配置
 * 声明秒杀订单队列、交换机及绑定关系
 */
@Configuration
public class SeckillRabbitMQConfig {

    /**
     * 秒杀订单 Direct 交换机
     */
    @Bean
    public DirectExchange seckillOrderDirect() {
        return new DirectExchange(QueueEnum.QUEUE_SECKILL_ORDER.getExchange());
    }

    /**
     * 秒杀订单队列（持久化）
     */
    @Bean
    public Queue seckillOrderQueue() {
        return new Queue(QueueEnum.QUEUE_SECKILL_ORDER.getName(), true);
    }

    /**
     * 绑定秒杀订单队列到交换机
     */
    @Bean
    public Binding seckillOrderBinding() {
        return BindingBuilder
                .bind(seckillOrderQueue())
                .to(seckillOrderDirect())
                .with(QueueEnum.QUEUE_SECKILL_ORDER.getRouteKey());
    }

    /**
     * 配置 JSON 消息转换器，替代默认的 SimpleMessageConverter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
