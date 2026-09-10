package com.mall.marketing.config;

import com.mall.marketing.domain.QueueEnum;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀 RabbitMQ 配置
 * - seckill.order：秒杀订单队列（已废弃，保留兼容）
 * - seckill.hold：秒杀占坑超时检查队列（TTL + DLX 延时消息模式，营销服务自闭环）
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

    /**
     * 消息转换器 Bean：Spring Boot 自动应用到监听容器，保证消费端用 JSON 反序列化
     * （否则 @RabbitListener 默认用 SimpleMessageConverter，会因自定义类不在白名单而失败）
     * 注意：Spring AMQP 3.2.x 的 trustedPackages 是「精确包名匹配」（非前缀），
     * 必须列出消息类的完整包名（如 com.mall.marketing.domain.dto），新增消息类需在此补充。
     */
    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages(
                "com.mall.marketing.domain.dto",
                "java.util", "java.lang", "java.math", "java.time", "java.io");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    // ============ 秒杀占坑超时检查队列（TTL + DLX） ============

    /** 占坑超时主交换机：接收 TTL 队列转发的过期消息 */
    @Bean
    public DirectExchange seckillHoldDirect() {
        return new DirectExchange(QueueEnum.QUEUE_SECKILL_HOLD.getExchange());
    }

    /** 占坑超时主队列：消费者监听（死信队列） */
    @Bean
    public Queue seckillHoldQueue() {
        return new Queue(QueueEnum.QUEUE_SECKILL_HOLD.getName(), true);
    }

    /** 主队列绑定到主交换机 */
    @Bean
    public Binding seckillHoldBinding() {
        return BindingBuilder
                .bind(seckillHoldQueue())
                .to(seckillHoldDirect())
                .with(QueueEnum.QUEUE_SECKILL_HOLD.getRouteKey());
    }

    /** 占坑超时 TTL 交换机：接收延时消息 */
    @Bean
    public DirectExchange seckillHoldTtlDirect() {
        return new DirectExchange(QueueEnum.QUEUE_TTL_SECKILL_HOLD.getExchange());
    }

    /** 占坑超时 TTL 队列：消息在此等待过期，过期后转发到主交换机 */
    @Bean
    public Queue seckillHoldTtlQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", QueueEnum.QUEUE_SECKILL_HOLD.getExchange());
        args.put("x-dead-letter-routing-key", QueueEnum.QUEUE_SECKILL_HOLD.getRouteKey());
        return QueueBuilder.durable(QueueEnum.QUEUE_TTL_SECKILL_HOLD.getName())
                .withArguments(args)
                .build();
    }

    /** TTL 队列绑定到 TTL 交换机 */
    @Bean
    public Binding seckillHoldTtlBinding() {
        return BindingBuilder
                .bind(seckillHoldTtlQueue())
                .to(seckillHoldTtlDirect())
                .with(QueueEnum.QUEUE_TTL_SECKILL_HOLD.getRouteKey());
    }
}
