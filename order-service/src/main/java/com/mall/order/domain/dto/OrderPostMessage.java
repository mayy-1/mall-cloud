package com.mall.order.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 下单后处理异步消息
 * 订单已同步创建完成后，将「标记优惠券已使用 / 扣减会员积分 / 清理购物车 / 发送延时取消订单消息」
 * 等耗时副作用通过 MQ 异步执行，以减少下单接口的响应时间。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPostMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 已创建的订单ID（用于发送延时取消订单消息） */
    private Long orderId;

    /** 会员ID */
    private Long memberId;

    /** 优惠券ID（未使用优惠券时为空） */
    private Long couponId;

    /** 本次下单使用的积分数（未使用积分时为空或 0） */
    private Integer useIntegration;

    /** 下单时会员的当前积分（用于计算扣减后的积分，兜底空值按 0 处理） */
    private Integer memberIntegration;

    /** 需要清理的购物车项ID列表（购物车下单才有，立即购买为空） */
    private List<Long> cartItemIds;
}
