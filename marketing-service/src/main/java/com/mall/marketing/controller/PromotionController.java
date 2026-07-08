package com.mall.marketing.controller;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 促销活动Feign调用Controller
 * 供cart-service通过Feign调用计算购物车促销信息
 */
@RestController
@RequestMapping("/promotion")
public class PromotionController {

    /**
     * 计算购物车促销信息（stub实现）
     * 当前直接返回输入列表，并设置默认促销字段
     */
    @PostMapping("/calcCartPromotion")
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> calcCartPromotion(@RequestBody List<Map<String, Object>> cartItemList) {
        for (Map<String, Object> item : cartItemList) {
            item.put("promotionMessage", "");
            item.put("reduceAmount", BigDecimal.ZERO);
            item.put("realStock", item.getOrDefault("stock", 0));
            item.put("integration", 0);
            item.put("growth", 0);
        }
        return cartItemList;
    }
}
