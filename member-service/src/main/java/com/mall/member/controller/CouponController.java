package com.mall.member.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.member.model.SmsCoupon;
import com.mall.member.model.SmsCouponHistory;
import com.mall.member.domain.dto.CartPromotionItem;
import com.mall.member.domain.dto.SmsCouponHistoryDetail;
import com.mall.member.service.ICouponService;
import com.mall.member.service.IMemberService;
import com.mall.api.client.CartClient;
import com.mall.api.dto.CartItemDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员优惠券管理Controller
 */
@RestController
@Tag(name = "CouponController", description = "用户优惠券管理")
@RequestMapping("/member/coupon")
@RequiredArgsConstructor
public class CouponController {
    /** 优惠券业务服务 */
    private final ICouponService couponService;
    /** 会员业务服务 */
    private final IMemberService memberService;
    /** 购物车Feign客户端 */
    private final CartClient cartClient;

    @Operation(summary = "领取指定优惠券")
    @PostMapping("/add/{couponId}")
    public CommonResult add(@PathVariable Long couponId) {
        couponService.add(couponId);
        return CommonResult.success(null,"领取成功");
    }

    @Operation(summary = "获取会员优惠券历史列表")
    @Parameter(name = "useStatus", description = "优惠券筛选类型:0->未使用；1->已使用；2->已过期",
            in = ParameterIn.QUERY,schema = @Schema(type = "integer",allowableValues = {"0","1","2"}))
    @GetMapping("/listHistory")
    public CommonResult<List<SmsCouponHistory>> listHistory(@RequestParam(value = "useStatus", required = false) Integer useStatus) {
        List<SmsCouponHistory> couponHistoryList = couponService.listHistory(useStatus);
        return CommonResult.success(couponHistoryList);
    }

    @Operation(summary = "获取会员优惠券列表")
    @Parameter(name = "useStatus", description = "优惠券筛选类型:0->未使用；1->已使用；2->已过期",
            in = ParameterIn.QUERY,schema = @Schema(type = "integer",allowableValues = {"0","1","2"}))
    @GetMapping("/list")
    public CommonResult<List<SmsCoupon>> list(@RequestParam(value = "useStatus", required = false) Integer useStatus) {
        List<SmsCoupon> couponList = couponService.list(useStatus);
        return CommonResult.success(couponList);
    }

    @Operation(summary = "获取登录会员购物车的相关优惠券")
    @Parameter(name = "type", description = "使用可用:0->不可用；1->可用",
            in = ParameterIn.PATH,schema = @Schema(type = "integer",defaultValue = "1",allowableValues = {"0","1"}))
    @GetMapping("/list/cart/{type}")
    public CommonResult<List<SmsCouponHistoryDetail>> listCart(@PathVariable Integer type) {
        List<CartPromotionItem> cartPromotionItemList;
        try {
            com.mym.mall.common.api.CommonResult<List<CartItemDTO>> cartResult = cartClient.list();
            if (cartResult != null && cartResult.getData() != null) {
                cartPromotionItemList = cartResult.getData().stream()
                        .map(this::convertToCartPromotionItem)
                        .collect(Collectors.toList());
            } else {
                cartPromotionItemList = List.of();
            }
        } catch (Exception e) {
            cartPromotionItemList = List.of();
        }
        List<SmsCouponHistoryDetail> couponHistoryList = couponService.listCart(cartPromotionItemList, type);
        return CommonResult.success(couponHistoryList);
    }

    private CartPromotionItem convertToCartPromotionItem(CartItemDTO cartItem) {
        CartPromotionItem item = new CartPromotionItem();
        item.setProductId(cartItem.getProductId());
        item.setQuantity(cartItem.getQuantity());
        item.setPrice(cartItem.getPrice());
        item.setReduceAmount(BigDecimal.ZERO);
        return item;
    }

    @Operation(summary = "获取当前商品相关优惠券")
    @GetMapping("/listByProduct/{productId}")
    public CommonResult<List<SmsCoupon>> listByProduct(@PathVariable Long productId) {
        List<SmsCoupon> couponHistoryList = couponService.listByProduct(productId);
        return CommonResult.success(couponHistoryList);
    }
}
