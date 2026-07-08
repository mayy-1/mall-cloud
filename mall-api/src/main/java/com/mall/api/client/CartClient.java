package com.mall.api.client;

import com.mall.api.dto.CartItemDTO;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车服务 Feign 接口
 */
@FeignClient(name = "cart-service", path = "/cart")
public interface CartClient {

    /** 获取购物车列表 */
    @GetMapping("/list")
    CommonResult<List<CartItemDTO>> list();

    /** 清空购物车 */
    @PostMapping("/clear")
    CommonResult<Void> clear();
}
