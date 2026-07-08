package com.mall.cart.feign;

import com.mall.cart.domain.dto.CartPromotionItem;
import com.mall.cart.model.OmsCartItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 促销服务 Feign 接口
 */
@FeignClient("marketing-service")
public interface OmsPromotionService {

    @PostMapping("/promotion/calcCartPromotion")
    List<CartPromotionItem> calcCartPromotion(@RequestBody List<OmsCartItem> cartItemList);
}
