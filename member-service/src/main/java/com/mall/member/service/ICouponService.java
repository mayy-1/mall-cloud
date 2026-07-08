package com.mall.member.service;

import com.mall.member.model.SmsCoupon;
import com.mall.member.model.SmsCouponHistory;
import com.mall.member.domain.dto.CartPromotionItem;
import com.mall.member.domain.dto.SmsCouponHistoryDetail;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户优惠券管理Service
 * Created by macro on 2018/8/29.
 */
public interface ICouponService {
    @Transactional
    void add(Long couponId);
    List<SmsCouponHistory> listHistory(Integer useStatus);
    List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type);
    List<SmsCoupon> listByProduct(Long productId);
    List<SmsCoupon> list(Integer useStatus);
}
