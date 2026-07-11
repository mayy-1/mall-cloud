package com.mall.api.client.marketing;

import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 秒杀 Feign 接口（会员端）
 */
@FeignClient(name = "marketing-service", path = "/seckill", contextId = "marketing-seckill")
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
