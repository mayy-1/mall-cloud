package com.mall.marketing.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 优惠券商品关联实体
 * 存储优惠券与商品的绑定关系
 */
@Data
public class SmsCouponProductRelation implements Serializable {
    private Long id;

    private Long couponId;

    private Long productId;

    @Schema(title = "商品名称")
    private String productName;

    @Schema(title = "商品编码")
    private String productSn;

    private static final long serialVersionUID = 1L;
}