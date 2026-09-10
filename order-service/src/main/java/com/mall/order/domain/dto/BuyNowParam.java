package com.mall.order.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 立即购买下单参数（无需购物车）
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BuyNowParam {
    /** 商品ID */
    private Long productId;
    /** 商品SKU ID */
    private Long productSkuId;
    /** 商品名称 */
    private String productName;
    /** 商品主图 */
    private String productPic;
    /** 商品副标题 */
    private String productSubTitle;
    /** SKU编码 */
    private String productSkuCode;
    /** 品牌名称 */
    private String productBrand;
    /** 商品分类ID */
    private Long productCategoryId;
    /** 商品货号 */
    private String productSn;
    /** 商品规格属性 */
    private String productAttr;
    /** 商品价格（用户看到的价格） */
    private BigDecimal price;
    /** 购买数量 */
    private Integer quantity;
}