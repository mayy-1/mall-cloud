package com.mall.product.domain.dto;

import com.mall.product.model.PmsProductAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 查询单个产品进行修改时返回的结果
 */
public class PmsProductResult extends PmsProductParam {
    @Getter
    @Setter
    @Schema(title = "商品所选分类的父id")
    private Long cateParentId;
    @Getter
    @Setter
    @Schema(title = "商品属性模板列表（规格+参数）")
    private List<PmsProductAttribute> productAttributeList;
}
