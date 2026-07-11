package com.mall.api.client;

import com.mall.api.dto.CartItemDTO;
import com.mall.api.dto.CartPromotionItemDTO;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 促销服务 Feign 接口
 */
@FeignClient(name = "marketing-service", path = "/promotion")
public interface PromotionClient {

    /** 计算购物车促销信息 */
    @PostMapping("/calcCartPromotion")
    CommonResult<List<CartPromotionItemDTO>> calcCartPromotion(@RequestBody List<CartItemDTO> cartItemList);
}
