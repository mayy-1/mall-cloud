package com.mall.api.dto;

import lombok.Data;

/**
 * SKU库存 Feign 传输对象
 */
@Data
public class SkuStockDTO {
    private Long id;
    private Long productId;
    private String skuCode;
    private Integer stock;
    private Integer lockStock;
}
