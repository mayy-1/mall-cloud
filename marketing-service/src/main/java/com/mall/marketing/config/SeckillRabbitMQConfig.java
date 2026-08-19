package com.mall.marketing.config;

import com.mall.marketing.domain.QueueEnum;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 秒杀 RabbitMQ 配置（生产者侧）
 * 只声明 Exchange 和 RabbitTemplate，Queue 与 Binding 由消费者侧（order-service）声明
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
     * 配置 JSON 消息转换器，替代默认的 SimpleMessageConverter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
