package com.mall.order.config;

import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息转换器配置
 * 统一使用 JSON 序列化，替代默认的 SimpleMessageConverter（JDK 序列化）。
 * 原因：Spring AMQP 3.x 默认禁止反序列化自定义类（JDK 反序列化安全限制），
 * 下单后处理消息 OrderPostMessage 等自定义对象会报 "unauthorized class"。
 * 改为 JSON 后与 marketing-service 的秒杀消息序列化方式保持一致。
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        // 注意：Spring AMQP 3.2.x 的 trustedPackages 是「精确包名匹配」（非前缀），
        // 必须列出消息类的完整包名（如 com.mall.order.domain.dto），新增消息类需在此补充。
        typeMapper.setTrustedPackages(
                "com.mall.order.domain.dto",
                "java.util", "java.lang", "java.math", "java.time", "java.io");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
