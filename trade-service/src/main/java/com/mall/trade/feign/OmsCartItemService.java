package com.mall.trade.feign;

import com.mall.trade.domain.dto.CartPromotionItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 购物车服务Feign客户端
 * 获取带促销信息的购物车列表及删除购物车商品
 */
@FeignClient("cart-service")
public interface OmsCartItemService {

    /** 获取含促销信息的购物车列表 */
    @GetMapping("/cart/list/promotion")
    List<CartPromotionItem> listPromotion(@RequestParam Long memberId, @RequestParam List<Long> cartIds);

    /** 删除购物车商品 */
    @PostMapping("/cart/delete")
    void delete(@RequestParam Long memberId, @RequestParam List<Long> ids);
}
