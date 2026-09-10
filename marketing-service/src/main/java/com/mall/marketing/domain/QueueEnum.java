package com.mall.marketing.domain;

import lombok.Getter;

/**
 * 营销服务消息队列枚举配置
 * 秒杀订单异步处理队列定义
 */
@Getter
public enum QueueEnum {
    /**
     * 秒杀订单队列（已废弃：确认建单改为同步接口，保留兼容）
     */
    QUEUE_SECKILL_ORDER("mall.seckill.direct", "mall.seckill.order", "mall.seckill.order"),
    /**
     * 秒杀占坑超时检查队列（死信队列，消费者监听）
     */
    QUEUE_SECKILL_HOLD("mall.seckill.hold.direct", "mall.seckill.hold", "mall.seckill.hold"),
    /**
     * 秒杀占坑超时检查 TTL 队列（延时消息在此等待过期）
     */
    QUEUE_TTL_SECKILL_HOLD("mall.seckill.hold.direct.ttl", "mall.seckill.hold.ttl", "mall.seckill.hold.ttl");

    /**
     * 交换名称
     */
    private final String exchange;
    /**
     * 队列名称
     */
    private final String name;
    /**
     * 路由键
     */
    private final String routeKey;

    QueueEnum(String exchange, String name, String routeKey) {
        this.exchange = exchange;
        this.name = name;
        this.routeKey = routeKey;
    }
}
