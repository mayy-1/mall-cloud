package com.mall.api.client.marketing;

import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 营销/促销 Feign 接口
 */
@FeignClient(name = "marketing-service", path = "/flash", contextId = "marketing-flash")
public interface MarketingClient {

    /** 获取当前秒杀活动 */
    @GetMapping("/current")
    CommonResult<Object> getCurrentFlashPromotion();

    /** 获取首页秒杀活动 */
    @GetMapping("/home")
    CommonResult<Object> getHomeFlashPromotion();
}


