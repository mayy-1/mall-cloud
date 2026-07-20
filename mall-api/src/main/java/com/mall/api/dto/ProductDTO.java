package com.mall.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品 Feign 传输对象（跨服务最小信息）
 */
@Data
public class ProductDTO {
    private Long id;
    private Long brandId;
    private String brandName;
    private Long productCategoryId;
    private String name;
    private String subTitle;
    private String pic;
    private BigDecimal price;
    private Integer stock;
    private Integer sale;
    private Integer newStatus;
    private Integer recommandStatus;
    private String productSn;
}
