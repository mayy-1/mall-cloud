package com.mall.marketing.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.marketing.domain.dto.CartPromotionItem;
import com.mall.marketing.domain.dto.SmsCouponHistoryDetail;
import com.mall.marketing.domain.dto.SmsCouponParam;
import com.mall.marketing.model.SmsCoupon;
import com.mall.marketing.model.SmsCouponHistory;
import com.mall.marketing.service.ICouponService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券管理Controller
 * Created by macro on 2018/8/28.
 */
@RestController
@Tag(name = "CouponController", description = "优惠券管理")
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {
    /** 优惠券服务 */
    private final ICouponService couponService;
    @Operation(summary = "添加优惠券")
    @PostMapping("/create")
    public CommonResult add(@RequestBody SmsCouponParam couponParam) {
        int count = couponService.create(couponParam);
        if(count>0){
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "删除优惠券")
    @PostMapping("/delete/{id}")
    public CommonResult delete(@PathVariable Long id) {
        int count = couponService.delete(id);
        if(count>0){
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改优惠券")
    @PostMapping("/update/{id}")
    public CommonResult update(@PathVariable Long id,@RequestBody SmsCouponParam couponParam) {
        int count = couponService.update(id,couponParam);
        if(count>0){
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "根据优惠券名称和类型分页获取优惠券列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<SmsCoupon>> list(
            @RequestParam(value = "name",required = false) String name,
            @RequestParam(value = "type",required = false) Integer type,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<SmsCoupon> couponList = couponService.list(name,type,pageSize,pageNum);
        return CommonResult.success(CommonPage.restPage(couponList));
    }

    @Operation(summary = "获取单个优惠券的详细信息")
    @GetMapping("/{id}")
    public CommonResult<SmsCouponParam> getItem(@PathVariable Long id) {
        SmsCouponParam couponParam = couponService.getItem(id);
        return CommonResult.success(couponParam);
    }

    // ==================== 用户端 ====================

    @Operation(summary = "会员领取指定优惠券")
    @PostMapping("/add/{couponId}")
    public CommonResult memberAdd(@PathVariable Long couponId) {
        couponService.add(couponId);
        return CommonResult.success(null, "领取成功");
    }

    @Operation(summary = "获取会员优惠券历史列表")
    @GetMapping("/member/listHistory")
    public CommonResult<List<SmsCouponHistory>> listHistory(@RequestParam(value = "useStatus", required = false) Integer useStatus) {
        List<SmsCouponHistory> list = couponService.listHistory(useStatus);
        return CommonResult.success(list);
    }

    @Operation(summary = "获取会员优惠券列表")
    @GetMapping("/member/list")
    public CommonResult<List<SmsCoupon>> memberList(@RequestParam(value = "useStatus", required = false) Integer useStatus) {
        List<SmsCoupon> list = couponService.listMemberCoupons(useStatus);
        return CommonResult.success(list);
    }

    @Operation(summary = "获取登录会员购物车的相关优惠券")
    @PostMapping("/member/list/cart/{type}")
    public CommonResult<List<SmsCouponHistoryDetail>> listCart(@PathVariable Integer type,
                                                                @RequestBody List<CartPromotionItem> cartPromotionItemList) {
        List<SmsCouponHistoryDetail> list = couponService.listCart(cartPromotionItemList, type);
        return CommonResult.success(list);
    }

    @Operation(summary = "获取当前商品相关优惠券")
    @GetMapping("/member/listByProduct/{productId}")
    public CommonResult<List<SmsCoupon>> listByProduct(@PathVariable Long productId) {
        List<SmsCoupon> list = couponService.listByProduct(productId);
        return CommonResult.success(list);
    }

    @Operation(summary = "更新优惠券使用状态（Feign内部调用）")
    @PostMapping("/member/updateStatus")
    public void updateCouponStatus(@RequestParam Long couponId,
                                   @RequestParam Long memberId,
                                   @RequestParam Integer useStatus) {
        couponService.updateCouponStatus(couponId, memberId, useStatus);
    }
}
