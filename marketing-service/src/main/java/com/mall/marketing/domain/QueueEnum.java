package com.mall.marketing.domain;

import lombok.Getter;

/**
 * 营销服务消息队列枚举配置
 * 秒杀订单异步处理队列定义
 */
@Getter
public enum QueueEnum {
    /**
     * 秒杀订单队列
     */
    QUEUE_SECKILL_ORDER("mall.seckill.direct", "mall.seckill.order", "mall.seckill.order");

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
