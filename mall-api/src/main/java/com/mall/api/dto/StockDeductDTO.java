package com.mall.api.dto;

import lombok.Data;

/**
 * 扣减库存请求参数
 */
@Data
public class StockDeductDTO {
    private Long productId;
    private Long skuId;
    private Integer quantity;
}
