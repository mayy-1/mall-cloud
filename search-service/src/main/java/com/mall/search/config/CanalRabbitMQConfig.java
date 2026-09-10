package com.mall.search.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Canal → RabbitMQ → ES 同步的消息队列配置
 */
@Configuration
public class CanalRabbitMQConfig {

    /** Canal 投递的交换机 */
    public static final String CANAL_EXCHANGE = "canal.exchange";
    /** 本服务监听的商品变更队列 */
    public static final String CANAL_ES_SYNC_QUEUE = "canal.es.sync";
    /** 路由 key*/
    public static final String CANAL_ROUTING_KEY = "example";

    @Bean
    public DirectExchange canalExchange() {
        return new DirectExchange(CANAL_EXCHANGE, true, false);
    }

    @Bean
    public Queue canalEsSyncQueue() {
        return new Queue(CANAL_ES_SYNC_QUEUE, true);
    }

    @Bean
    public Binding canalEsSyncBinding() {
        return BindingBuilder.bind(canalEsSyncQueue())
                .to(canalExchange())
                .with(CANAL_ROUTING_KEY);
    }
}
