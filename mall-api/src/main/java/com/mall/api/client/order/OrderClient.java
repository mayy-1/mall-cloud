package com.mall.api.client.order;

import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单服务 Feign 接口
 */
@FeignClient(name = "order-service", path = "/order", contextId = "order")
public interface OrderClient {

    /** 根据订单编号处理支付成功回调 */
    @PostMapping("/paySuccessByOrderSn")
    CommonResult<Void> paySuccessByOrderSn(@RequestParam String orderSn, @RequestParam Integer payType);
}
