package com.mall.marketing.service.impl;

import com.mall.api.client.product.ProductClient;
import com.mall.api.dto.CartItemDTO;
import com.mall.api.dto.CartItemDetailDTO;
import com.mall.api.dto.ProductDTO;
import com.mall.marketing.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 购物车促销计算 Service 实现
 * <p>
 * 仅计算普通商品的价格/库存/积分。
 * 秒杀不经过购物车，走"立即购买"独立入口（SeckillController）。
 */
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    /** 商品服务 Feign */
    private final ProductClient productClient;

    @Override
    public List<CartItemDetailDTO> calcCartPromotion(List<CartItemDTO> cartItemList) {
        if (cartItemList == null || cartItemList.isEmpty()) {
            return Collections.emptyList();
        }
        List<CartItemDetailDTO> result = new ArrayList<>();
        for (CartItemDTO item : cartItemList) {
            CartItemDetailDTO promoItem = new CartItemDetailDTO();
            BeanUtils.copyProperties(item, promoItem);
            promoItem.setRealStock(fetchProductStock(item.getProductId()));
            result.add(promoItem);
        }
        return result;
    }

    /**
     * 从商品服务查询库存
     */
    private Integer fetchProductStock(Long productId) {
        try {
            ProductDTO product = productClient.getById(productId).getData();
            return product != null && product.getStock() != null ? product.getStock() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}