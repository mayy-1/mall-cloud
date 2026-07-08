package com.mall.marketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀订单异步消息
 * Lua 扣减成功后发送到 RabbitMQ，由 trade-service 消费并创建订单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 秒杀活动ID */
    private Long promotionId;

    /** 秒杀场次ID */
    private Long sessionId;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 会员ID */
    private Long memberId;

    /** 会员昵称 */
    private String memberNickname;

    /** 秒杀价格 */
    private BigDecimal seckillPrice;

    /** 购买数量（当前默认1件） */
    private Integer quantity;

    /** 下单时间 */
    private Date createTime;
}
