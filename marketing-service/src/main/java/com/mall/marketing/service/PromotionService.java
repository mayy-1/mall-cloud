package com.mall.marketing.service;

import com.mall.api.dto.CartItemDTO;
import com.mall.api.dto.CartItemDetailDTO;

import java.util.List;

/**
 * 促销计算服务接口
 * <p>
 * 供购物车模块通过 Feign 调用，计算每件商品的秒杀价、真实库存、积分等信息。
 */
public interface PromotionService {

    /**
     * 计算购物车每件商品的促销信息
     *
     * @param cartItemList 购物车商品列表
     * @return 含促销信息的购物车商品列表
     */
    List<CartItemDetailDTO> calcCartPromotion(List<CartItemDTO> cartItemList);
}