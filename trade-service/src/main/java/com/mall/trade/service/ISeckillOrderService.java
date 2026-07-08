package com.mall.trade.service;

import com.mall.trade.domain.dto.SeckillOrderMessage;

/**
 * 秒杀订单服务接口
 * 处理秒杀异步下单：创建订单、扣减库存、记录日志
 */
public interface ISeckillOrderService {

    /**
     * 创建秒杀订单
     * 使用 Redisson 分布式锁作为兜底保护，防止极端并发下的重复下单
     *
     * @param message 秒杀订单消息
     * @return 订单ID
     */
    Long createSeckillOrder(SeckillOrderMessage message);

    /**
     * 回滚秒杀库存
     * 当秒杀订单被取消或超时关闭时，将 Redis 中的秒杀库存恢复
     *
     * @param promotionId 秒杀活动ID
     * @param productId   商品ID
     * @param memberId    会员ID
     * @param quantity    回滚数量
     */
    void rollbackSeckillStock(Long promotionId, Long productId, Long memberId, Integer quantity);
}
