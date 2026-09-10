package com.mall.marketing.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 秒杀占坑超时检查消息
 * 用户抢购占坑后发送延时消息，超时未确认支付时触发回滚
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillHoldMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 秒杀活动ID */
    private Long promotionId;

    /** 商品ID */
    private Long productId;

    /** 会员ID */
    private Long memberId;

    /** 购买数量 */
    private Integer quantity;
}
