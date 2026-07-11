package com.mall.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项 Feign 传输对象
 */
@Data
public class CartItemDTO {
    private Long id;
    private Long productId;
    private Long productSkuId;
    private Long memberId;
    private Integer quantity;
    private BigDecimal price;
    private String productName;
    private String productPic;
    private String productSubTitle;
    private String productSkuCode;
    private String memberNickname;
    private String sp1;
    private String sp2;
    private String sp3;
    private Long productCategoryId;
    private String productBrand;
    private String productSn;
    private String productAttr;
}
