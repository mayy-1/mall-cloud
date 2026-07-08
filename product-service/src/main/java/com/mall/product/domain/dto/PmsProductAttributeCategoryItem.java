package com.mall.product.domain.dto;

import com.mall.product.model.PmsProductAttribute;
import com.mall.product.model.PmsProductAttributeCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 包含有分类下属性的dto
 * Created by macro on 2018/5/24.
 */
public class PmsProductAttributeCategoryItem extends PmsProductAttributeCategory {
    @Getter
    @Setter
    @Schema(title = "商品属性列表")
    private List<PmsProductAttribute> productAttributeList;
}
