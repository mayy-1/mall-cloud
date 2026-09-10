package com.mall.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车订单项明细（下单用）
 * <p>承载购物车商品的下单全链路数据：前端展示 + 库存校验 + 订单入库</p>
 */
@Data
public class CartItemDetailDTO {
    /** 购物车项 ID */
    private Long id;
    /** 商品 SPU ID */
    private Long productId;
    /** 商品 SKU ID（锁库存用） */
    private Long productSkuId;
    /** 会员 ID（删除购物车记录用） */
    private Long memberId;
    /** 购买数量 */
    private Integer quantity;
    /** 商品单价（后端查 DB 覆盖，防前端篡改） */
    private BigDecimal price;
    /** 商品主图 URL（确认单展示用） */
    private String productPic;
    /** 商品名称（确认单展示用） */
    private String productName;
    /** SKU 编码（锁库存时传给 product-service） */
    private String productSkuCode;
    /** 商品分类 ID（优惠券匹配用） */
    private Long productCategoryId;
    /** 商品品牌（订单明细入库用） */
    private String productBrand;
    /** 商品货号（订单明细入库用） */
    private String productSn;
    /** 商品规格属性（确认单展示用，如"颜色:红;尺寸:L"） */
    private String productAttr;
    /** 真实可售库存（下单时查 product-service，用于 hasStock 校验） */
    private Integer realStock;
    /** 赠送的积分（下单时从商品查，落订单项） */
    private Integer giftIntegration;
    /** 赠送的成长值（下单时从商品查，落订单项） */
    private Integer giftGrowth;
}
