package com.mall.marketing.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 优惠券商品分类关联实体
 * 存储优惠券与商品分类的绑定关系
 */
@Data
public class SmsCouponProductCategoryRelation implements Serializable {
    private Long id;

    private Long couponId;

    private Long productCategoryId;

    @Schema(title = "产品分类名称")
    private String productCategoryName;

    @Schema(title = "父分类名称")
    private String parentCategoryName;

    private static final long serialVersionUID = 1L;
}