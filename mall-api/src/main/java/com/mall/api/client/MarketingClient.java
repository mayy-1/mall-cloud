package com.mall.api.client;

import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 营销/促销 Feign 接口
 */
@FeignClient(name = "marketing-service", path = "/flash")
public interface MarketingClient {

    /** 获取当前秒杀活动 */
    @GetMapping("/current")
    CommonResult<Object> getCurrentFlashPromotion();

    /** 获取首页秒杀活动 */
    @GetMapping("/home")
    CommonResult<Object> getHomeFlashPromotion();
}

/**
 * 秒杀 Feign 接口（会员端）
 */
@FeignClient(name = "marketing-service", path = "/seckill")
interface SeckillClient {

    /** 查询秒杀商品剩余库存 */
    @GetMapping("/stock")
    CommonResult<Long> getSeckillStock(@RequestParam("promotionId") Long promotionId,
                                         @RequestParam("productId") Long productId);

    /** 获取当前秒杀活动列表 */
    @GetMapping("/list")
    CommonResult<Object> getCurrentSeckillProducts();

    /** 获取秒杀商品详情 */
    @GetMapping("/detail")
    CommonResult<Object> getSeckillProductDetail(@RequestParam("promotionId") Long promotionId,
                                                  @RequestParam("productId") Long productId);
}
