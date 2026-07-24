package com.mall.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 购物车商品规格选择 Feign 传输对象
 * 用户在购物车中"重选规格"时使用
 */
@Data
public class CartProductDTO {
    /** 商品基础信息 */
    private Long productId;
    private String name;
    private String subTitle;
    private String pic;
    private String brandName;
    /** 商品属性列表（规格选项） */
    private List<ProductAttributeDTO> productAttributeList;
    /** SKU 库存列表 */
    private List<SkuStockDTO> skuStockList;
}
