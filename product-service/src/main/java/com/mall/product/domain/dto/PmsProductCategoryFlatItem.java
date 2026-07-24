package com.mall.product.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 分类树扁平结构（一级分类 + 子分类）
 * 对应 SQL: listWithChildren
 * 用于在 Service 层手动组装树形结构
 */
@Getter
@Setter
public class PmsProductCategoryFlatItem {

    @Schema(title = "一级分类ID")
    private Long id;

    @Schema(title = "一级分类名称")
    private String name;

    @Schema(title = "子分类ID")
    private Long childId;

    @Schema(title = "子分类名称")
    private String childName;
}
