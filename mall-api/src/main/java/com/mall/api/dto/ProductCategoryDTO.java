package com.mall.api.dto;

import lombok.Data;

/**
 * 商品分类 Feign 传输对象（跨服务最小信息）
 */
@Data
public class ProductCategoryDTO {
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer productCount;
    private String productUnit;
    private Integer navStatus;
    private Integer showStatus;
    private Integer sort;
    private String icon;
    private String keywords;
    private String description;
}
