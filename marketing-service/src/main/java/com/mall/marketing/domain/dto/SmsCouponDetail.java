package com.mall.marketing.domain.dto;

import com.mall.marketing.model.SmsCoupon;
import com.mall.marketing.model.SmsCouponProductCategoryRelation;
import com.mall.marketing.model.SmsCouponProductRelation;

import java.util.List;

/**
 * 优惠券详情（含关联商品/分类，无历史领取记录字段）
 */
public class SmsCouponDetail {
    /** 优惠券基础信息 */
    private SmsCoupon coupon;
    /** 关联商品列表（useType=2） */
    private List<SmsCouponProductRelation> productRelationList;
    /** 关联分类列表（useType=1） */
    private List<SmsCouponProductCategoryRelation> categoryRelationList;

    public SmsCoupon getCoupon() {
        return coupon;
    }

    public void setCoupon(SmsCoupon coupon) {
        this.coupon = coupon;
    }

    public List<SmsCouponProductRelation> getProductRelationList() {
        return productRelationList;
    }

    public void setProductRelationList(List<SmsCouponProductRelation> productRelationList) {
        this.productRelationList = productRelationList;
    }

    public List<SmsCouponProductCategoryRelation> getCategoryRelationList() {
        return categoryRelationList;
    }

    public void setCategoryRelationList(List<SmsCouponProductCategoryRelation> categoryRelationList) {
        this.categoryRelationList = categoryRelationList;
    }
}