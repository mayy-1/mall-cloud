package com.mall.order.domain.dto;

import lombok.Data;

/**
 * 秒杀订单确认参数
 * 用户抢购占坑后，在确认订单页选择收货地址并提交，生成正式订单
 */
@Data
public class SeckillConfirmParam {

    /** 秒杀活动ID */
    private Long promotionId;

    /** 商品ID */
    private Long productId;

    /** 收货地址ID */
    private Long memberReceiveAddressId;

    /** 支付方式 */
    private Integer payType;
}
