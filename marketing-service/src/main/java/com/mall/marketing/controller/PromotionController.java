package com.mall.marketing.controller;

import com.mall.api.dto.CartItemDTO;
import com.mall.api.dto.CartItemDetailDTO;
import com.mall.marketing.service.PromotionService;
import com.mym.mall.common.api.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车促销计算 Controller
 */
@RestController
@RequestMapping("/promotion")
@RequiredArgsConstructor
public class PromotionController {

    /** 促销计算服务 */
    private final PromotionService promotionService;

    /**
     * 计算购物车每件商品的促销信息
     * @param cartItemList 购物车商品列表（由 cart-service 组装）
     * @return 含促销信息的购物车商品列表
     */
    @PostMapping("/calcCartPromotion")
    public CommonResult<List<CartItemDetailDTO>> calcCartPromotion(@RequestBody List<CartItemDTO> cartItemList) {
        List<CartItemDetailDTO> result = promotionService.calcCartPromotion(cartItemList);
        return CommonResult.success(result);
    }
}