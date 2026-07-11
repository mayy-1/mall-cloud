package com.mall.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 含促销信息的购物车项 Feign 传输对象
 */
@Data
public class CartPromotionItemDTO {
    private Long id;
    private Long productId;
    private Long productSkuId;
    private Long memberId;
    private Integer quantity;
    private BigDecimal price;
    private String productPic;
    private String productName;
    private String productSubTitle;
    private String productSkuCode;
    private String memberNickname;
    private Long productCategoryId;
    private String productBrand;
    private String productSn;
    private String productAttr;
    private String promotionMessage;
    private BigDecimal reduceAmount;
    private Integer realStock;
    private Integer integration;
    private Integer growth;
}
