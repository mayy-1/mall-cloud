package com.mall.order.domain;

import lombok.Getter;

/**
 * 秒杀订单消息队列枚举
 */
@Getter
public enum SecKillQueueEnum {
    /**
     * 秒杀订单队列
     */
    QUEUE_SECKILL_ORDER("mall.seckill.direct", "mall.seckill.order", "mall.seckill.order");

    private final String exchange;
    private final String name;
    private final String routeKey;

    SecKillQueueEnum(String exchange, String name, String routeKey) {
        this.exchange = exchange;
        this.name = name;
        this.routeKey = routeKey;
    }
}

