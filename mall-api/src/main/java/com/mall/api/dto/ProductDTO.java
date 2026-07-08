package com.mall.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品 Feign 传输对象（跨服务最小信息）
 */
@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String pic;
    private BigDecimal price;
    private Integer stock;
    private String brandName;
    private String productSn;
}
