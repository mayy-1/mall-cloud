package com.mall.api.client.marketing;

import com.mall.api.dto.CartPromotionItemDTO;
import com.mall.api.dto.CouponHistoryDetailDTO;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 优惠券服务 Feign 接口（调用 marketing-service 前台端点）
 */
@FeignClient(name = "marketing-service", path = "/coupon/member", contextId = "marketing-coupon-member")
public interface MarketingCouponClient {

    /** 获取购物车可用优惠券列表 */
    @PostMapping("/list/cart/{type}")
    CommonResult<List<CouponHistoryDetailDTO>> listCart(@RequestBody List<CartPromotionItemDTO> items,
                                                        @PathVariable("type") Integer type);

    /** 更新优惠券使用状态 */
    @PostMapping("/updateStatus")
    void updateCouponStatus(@RequestParam Long couponId,
                            @RequestParam Long memberId,
                            @RequestParam Integer useStatus);
}
