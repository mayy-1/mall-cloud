package com.mall.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 优惠券历史详情 Feign 传输对象
 */
@Data
public class CouponHistoryDetailDTO {
    private Long id;
    private CouponDTO coupon;
    private List<CouponProductRelationDTO> productRelationList;
    private List<CouponProductCategoryRelationDTO> categoryRelationList;
}
