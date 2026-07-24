package com.mall.api.dto;

import lombok.Data;

/**
 * 商品属性 Feign 传输对象
 */
@Data
public class ProductAttributeDTO {
    private Long id;
    private Long productAttributeCategoryId;
    private String name;
    private Integer selectType;
    private Integer inputType;
    private String inputList;
    private Integer sort;
    private Integer filterType;
    private Integer type;
}
