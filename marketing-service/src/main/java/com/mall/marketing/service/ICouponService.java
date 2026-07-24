package com.mall.marketing.service;

import com.mall.marketing.domain.dto.CartPromotionItem;
import com.mall.marketing.domain.dto.SmsCouponDetail;
import com.mall.marketing.domain.dto.SmsCouponHistoryDetail;
import com.mall.marketing.domain.dto.SmsCouponParam;
import com.mall.marketing.model.SmsCoupon;
import com.mall.marketing.model.SmsCouponHistory;

import java.util.List;

/**
 * 优惠券管理Service
 */
public interface ICouponService {
    /** 添加优惠券 */
    int create(SmsCouponParam couponParam);

    /** 根据优惠券id删除优惠券 */
    int delete(Long id);

    /** 根据优惠券id更新优惠券信息 */
    int update(Long id, SmsCouponParam couponParam);

    /** 更新优惠券上下架状态 */
    int updateStatus(Long id, Integer status);

    /** 分页获取优惠券列表 */
    List<SmsCoupon> list(String name, Integer type, Integer status, Integer pageSize, Integer pageNum);

    /** 获取优惠券详情 */
    SmsCouponParam getItem(Long id);

    // ==================== 用户端 ====================

    /** 会员领取优惠券 */
    void add(Long couponId);

    /** 获取会员优惠券领取历史 */
    List<SmsCouponHistoryDetail> listHistory(Integer useStatus);

    /** 获取购物车可用优惠券列表 */
    List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type);

    /** 获取指定商品可用优惠券列表 */
    List<SmsCoupon> listByProduct(Long productId);


    /** 更新优惠券使用状态 */
    void updateCouponStatus(Long couponId, Long memberId, Integer useStatus);

    /** 平台所有可领优惠券（含关联分类/商品） */
    List<SmsCouponDetail> listAvailableCoupons();

    List<SmsCoupon> listCoupons();
}
