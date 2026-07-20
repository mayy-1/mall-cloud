package com.mall.product.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品查询参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PmsProductQueryParam {
    @Schema(title = "上架状态")
    private Integer publishStatus;
    @Schema(title = "审核状态")
    private Integer verifyStatus;
    @Schema(title = "商品名称模糊关键字")
    private String keyword;
    @Schema(title = "商品货号")
    private String productSn;
    @Schema(title = "商品分类编号")
    private Long productCategoryId;
    @Schema(title = "商品品牌编号")
    private Long brandId;
    @Schema(title = "新品状态:0->不是新品；1->新品")
    private Integer newStatus;
    @Schema(title = "推荐状态；0->不推荐；1->推荐")
    private Integer recommandStatus;
    @Schema(title = "删除状态：0->未删除；1->已删除")
    private Integer deleteStatus;
}
