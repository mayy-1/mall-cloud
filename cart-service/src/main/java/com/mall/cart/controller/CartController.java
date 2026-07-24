package com.mall.cart.controller;

import com.mall.api.client.member.MemberClient;
import com.mall.api.dto.CartPromotionItemDTO;
import com.mall.cart.domain.dto.CartProduct;
import com.mall.cart.model.OmsCartItem;
import com.mall.cart.service.ICartService;
import com.mym.mall.common.api.CommonResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车管理Controller
 */
@RestController
@Tag(name = "CartController", description = "购物车管理")
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    /** 购物车业务服务 */
    private final ICartService cartItemService;
    /** 会员信息Feign服务 */
    private final MemberClient memberClient;

    /**
     * 添加商品到购物车
     * POST /cart/add
     */
    @Operation(summary = "添加商品到购物车")
    @PostMapping("/add")
    public CommonResult add(@RequestBody OmsCartItem cartItem) {
        int count = cartItemService.add(cartItem);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 获取当前会员购物车列表
     * GET /cart/list
     */
    @Operation(summary = "获取某个会员的购物车列表")
    @GetMapping("/list")
    public CommonResult<List<OmsCartItem>> list() {
        List<OmsCartItem> cartItemList = cartItemService.list(memberClient.getCurrentMember().getData().getId());
        return CommonResult.success(cartItemList);
    }

    /**
     * 获取购物车列表（含促销信息，下单前用）
     * GET /cart/list/promotion
     */
    @Operation(summary = "获取某个会员的购物车列表,包括促销信息")
    @GetMapping("/list/promotion")
    public CommonResult<List<CartPromotionItemDTO>> listPromotion(@RequestParam(required = false) List<Long> cartIds) {
        List<CartPromotionItemDTO> cartPromotionItemList = cartItemService.listPromotion(memberClient.getCurrentMember().getData().getId(), cartIds);
        return CommonResult.success(cartPromotionItemList);
    }

    /**
     * 修改购物车商品数量
     * GET /cart/update/quantity?id=1&quantity=2
     */
    @Operation(summary = "修改购物车中某个商品的数量")
    @GetMapping("/update/quantity")
    public CommonResult updateQuantity(@RequestParam Long id,
                                       @RequestParam Integer quantity) {
        int count = cartItemService.updateQuantity(id, memberClient.getCurrentMember().getData().getId(), quantity);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 获取商品可重选规格（购物车规格变更用）
     * GET /cart/getProduct/{productId}
     */
    @Operation(summary = "获取购物车中某个商品的规格,用于重选规格")
    @GetMapping("/getProduct/{productId}")
    public CommonResult<CartProduct> getCartProduct(@PathVariable Long productId) {
        CartProduct cartProduct = cartItemService.getCartProduct(productId);
        return CommonResult.success(cartProduct);
    }

    /**
     * 修改购物车商品规格
     * POST /cart/update/attr
     */
    @Operation(summary = "修改购物车中商品的规格")
    @PostMapping("/update/attr")
    public CommonResult updateAttr(@RequestBody OmsCartItem cartItem) {
        int count = cartItemService.updateAttr(cartItem);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 删除购物车商品
     * POST /cart/delete?ids=1,2,3
     */
    @Operation(summary = "删除购物车中的某个商品")
    @PostMapping("/delete")
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {
        int count = cartItemService.delete(memberClient.getCurrentMember().getData().getId(), ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 清空当前会员购物车
     * POST /cart/clear
     */
    @Operation(summary = "清空购物车")
    @PostMapping("/clear")
    public CommonResult clear() {
        int count = cartItemService.clear(memberClient.getCurrentMember().getData().getId());
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
