package com.mall.api.client;

import com.mall.api.dto.CartPromotionItemDTO;
import com.mall.api.dto.CouponHistoryDetailDTO;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 会员优惠券服务 Feign 接口
 */
@FeignClient(name = "member-service", path = "/member/coupon")
public interface MemberCouponClient {

    /** 获取购物车可用优惠券列表 */
    @PostMapping("/list/cart")
    CommonResult<List<CouponHistoryDetailDTO>> listCart(@RequestBody List<CartPromotionItemDTO> items, @RequestParam Integer type);
}
