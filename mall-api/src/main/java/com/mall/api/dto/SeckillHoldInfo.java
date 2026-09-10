package com.mall.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀占坑信息（存于 Redis 占坑标记 seckill:hold:{promotionId}:{productId}:{memberId}）
 * 抢购占坑成功后写入，确认建单时读取，超时未确认则回滚
 */
@Data
public class SeckillHoldInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** SKU编号 */
    private Long skuId;

    /** 秒杀价格 */
    private BigDecimal seckillPrice;

    /** 购买数量 */
    private Integer quantity;

    /** 商品名称 */
    private String productName;
}
