package com.mall.marketing.service;

import com.mall.marketing.domain.dto.CartPromotionItem;
import com.mall.marketing.domain.dto.SmsCouponHistoryDetail;
import com.mall.marketing.domain.dto.SmsCouponParam;
import com.mall.marketing.model.SmsCoupon;
import com.mall.marketing.model.SmsCouponHistory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 优惠券管理Service
 * Created by macro on 2018/8/28.
 */
public interface ICouponService {
    /**
     * 添加优惠券
     */
    @Transactional
    int create(SmsCouponParam couponParam);

    /**
     * 根据优惠券id删除优惠券
     */
    @Transactional
    int delete(Long id);

    /**
     * 根据优惠券id更新优惠券信息
     */
    @Transactional
    int update(Long id, SmsCouponParam couponParam);

    /**
     * 分页获取优惠券列表
     */
    List<SmsCoupon> list(String name, Integer type, Integer pageSize, Integer pageNum);

    /**
     * 获取优惠券详情
     * @param id 优惠券表id
     */
    SmsCouponParam getItem(Long id);

    // ==================== 用户端 ====================

    /** 会员领取优惠券 */
    void add(Long couponId);

    /** 获取会员优惠券领取历史 */
    List<SmsCouponHistory> listHistory(Integer useStatus);

    /** 获取购物车可用优惠券列表 */
    List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type);

    /** 获取指定商品可用优惠券列表 */
    List<SmsCoupon> listByProduct(Long productId);

    /** 获取会员当前可用优惠券 */
    List<SmsCoupon> listMemberCoupons(Integer useStatus);

    /** 更新优惠券使用状态 */
    void updateCouponStatus(Long couponId, Long memberId, Integer useStatus);
}
