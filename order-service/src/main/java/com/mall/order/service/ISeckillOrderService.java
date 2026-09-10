package com.mall.order.service;

import com.mall.order.domain.dto.SeckillConfirmParam;

/**
 * 秒杀订单服务接口
 * 处理秒杀确认下单：校验占坑、创建订单（含收货地址）、扣减库存、回滚
 */
public interface ISeckillOrderService {

    /**
     * 确认秒杀订单（占坑后选地址提交）
     * 校验占坑标记 → 生成订单（含收货地址）→ 删除占坑标记 → 发延时取消消息
     *
     * @param param 确认参数（活动ID、商品ID、收货地址ID、支付方式）
     * @return 订单ID
     */
    Long confirmSeckillOrder(SeckillConfirmParam param);

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

