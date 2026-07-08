package com.mall.trade.feign;

import com.mall.trade.domain.dto.CartPromotionItem;
import com.mall.trade.domain.dto.SmsCouponHistoryDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 会员优惠券服务Feign客户端
 * 获取会员购物车可用优惠券列表
 */
@FeignClient("member-service")
public interface UmsMemberCouponService {

    /** 获取购物车可用优惠券列表 */
    @PostMapping("/member/coupon/list/cart")
    List<SmsCouponHistoryDetail> listCart(@RequestBody List<CartPromotionItem> items, @RequestParam Integer type);
}
