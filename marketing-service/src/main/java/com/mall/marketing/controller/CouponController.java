package com.mall.marketing.controller;

import com.mall.marketing.domain.dto.SmsCouponDetail;
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
 * 优惠券管理 Controller
 */
@RestController
@Tag(name = "CouponController", description = "优惠券管理")
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    /** 优惠券业务服务 */
    private final ICouponService couponService;

    // ==================== 管理端接口 ====================

    /**
     * 新增优惠券
     */
    @Operation(summary = "添加优惠券")
    @PostMapping("/create")
    public CommonResult add(@RequestBody SmsCouponParam couponParam) {
        int count = couponService.create(couponParam);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 删除优惠券（物理删除）
     * @param id 优惠券 ID
     * @return 受影响行数
     */
    @Operation(summary = "删除优惠券")
    @PostMapping("/delete/{id}")
    public CommonResult delete(@PathVariable Long id) {
        int count = couponService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 修改优惠券
     * 先删除旧关联，再插入新关联，保证关联数据与 useType 一致。
     * @param id          优惠券 ID
     * @param couponParam 修改后的优惠券参数
     * @return 受影响行数
     */
    @Operation(summary = "修改优惠券")
    @PostMapping("/update/{id}")
    public CommonResult update(@PathVariable Long id, @RequestBody SmsCouponParam couponParam) {
        int count = couponService.update(id, couponParam);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 切换优惠券上下架状态
     * @return 受影响行数
     */
    @Operation(summary = "修改优惠券上下架状态")
    @PostMapping("/update/status/{id}")
    public CommonResult updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        int count = couponService.updateStatus(id, status);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 管理端分页查询优惠券列表
     * @return 分页结果
     */
    @Operation(summary = "根据优惠券名称、类型和状态分页获取优惠券列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<SmsCoupon>> list(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<SmsCoupon> couponList = couponService.list(name, type, status, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(couponList));
    }

    /**
     * 获取优惠券详情（含关联商品/分类）
     */
    @Operation(summary = "获取单个优惠券的详细信息")
    @GetMapping("/{id}")
    public CommonResult<SmsCouponParam> getItem(@PathVariable Long id) {
        SmsCouponParam couponParam = couponService.getItem(id);
        return CommonResult.success(couponParam);
    }

    // ==================== 用户端接口 ====================

    /**
     * 会员领取优惠券
     * 调用方：前台领券中心"立即领取"按钮。
     * 校验逻辑：库存是否充足、是否在有效期内、是否超过限领次数。
     *
     * @param couponId 优惠券 ID
     */
    @Operation(summary = "会员领取指定优惠券")
    @PostMapping("/add/{couponId}")
    public CommonResult memberAdd(@PathVariable Long couponId) {
        couponService.add(couponId);
        return CommonResult.success(null, "领取成功");
    }

    /**
     * 查询会员已领取的优惠券历史（用于"我的优惠券"页面）
     * <p>
     * 返回 SmsCouponHistoryDetail，包含领取记录 + 嵌套的优惠券信息 + 关联商品/分类。
     *
     * @param useStatus 使用状态：0-未使用，1-已使用，2-已过期（可选，不传则查全部）
     * @return 已领取优惠券列表（含优惠券详情和关联数据）
     */
    @Operation(summary = "获取会员优惠券历史列表")
    @GetMapping("/member/listHistory")
    public CommonResult<List<SmsCouponHistoryDetail>> listHistory(
            @RequestParam(value = "useStatus", required = false) Integer useStatus) {
        List<SmsCouponHistoryDetail> list = couponService.listHistory(useStatus);
        return CommonResult.success(list);
    }

    /**
     * 查询平台所有可领取的优惠券（用于"领券中心"页面）
     */
    @Operation(summary = "获取优惠券列表")
    @GetMapping("/member/list")
    public CommonResult<List<SmsCouponDetail>> couponsList() {
        List<SmsCouponDetail> list = couponService.listAvailableCoupons();
        return CommonResult.success(list);
    }

    /**
     * 获取当前会员购物车中可用的优惠券（用于下单确认页选择优惠券）
     */
    @Operation(summary = "获取登录会员购物车的相关优惠券")
    @PostMapping("/member/list/cart/{type}")
    public CommonResult<List<SmsCouponHistoryDetail>> listCart(
            @PathVariable Integer type,
            @RequestBody List<CartPromotionItem> cartPromotionItemList) {
        List<SmsCouponHistoryDetail> list = couponService.listCart(cartPromotionItemList, type);
        return CommonResult.success(list);
    }

    /**
     * 查询指定商品可用的优惠券（用于商品详情页）
     * <p>
     * 根据商品 ID 和商品分类，查出 useType=0（全场通用）以及 useType=1/2（匹配该商品或分类）的优惠券。
     *
     * @param productId 商品 ID
     * @return 可用优惠券列表
     */
    @Operation(summary = "获取当前商品相关优惠券")
    @GetMapping("/member/listByProduct/{productId}")
    public CommonResult<List<SmsCoupon>> listByProduct(@PathVariable Long productId) {
        List<SmsCoupon> list = couponService.listByProduct(productId);
        return CommonResult.success(list);
    }

    // ==================== Feign 内部调用 ====================

    /**
     * 更新会员优惠券使用状态（仅供 order-service 通过 Feign 调用）
     * <p>
     * 下单成功后将优惠券标记为"已使用"；订单取消后回滚为"未使用"。
     *
     * @param couponId  优惠券 ID
     * @param memberId  会员 ID
     * @param useStatus 使用状态：0-未使用，1-已使用
     */
    @Operation(summary = "更新优惠券使用状态（Feign内部调用）")
    @PostMapping("/member/updateStatus")
    public void updateCouponStatus(@RequestParam Long couponId,
                                   @RequestParam Long memberId,
                                   @RequestParam Integer useStatus) {
        couponService.updateCouponStatus(couponId, memberId, useStatus);
    }
}