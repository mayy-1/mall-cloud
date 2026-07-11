package com.mall.marketing.domain.dto;

import com.mall.marketing.model.SmsCoupon;
import com.mall.marketing.model.SmsCouponHistory;
import com.mall.marketing.model.SmsCouponProductCategoryRelation;
import com.mall.marketing.model.SmsCouponProductRelation;

import java.util.List;

/**
 * 优惠券领取历史详情封装
 */
public class SmsCouponHistoryDetail extends SmsCouponHistory {
    private SmsCoupon coupon;
    private List<SmsCouponProductRelation> productRelationList;
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
