package com.mall.order.controller;

import com.mall.order.domain.dto.SeckillConfirmParam;
import com.mall.order.service.ISeckillOrderService;
import com.mym.mall.common.api.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台秒杀订单控制器 —— 秒杀占坑后的确认下单
 */
@RestController
@Tag(name = "PortalSeckillOrderController", description = "前台秒杀订单管理")
@RequestMapping("/order")
@RequiredArgsConstructor
public class PortalSeckillOrderController {

    private final ISeckillOrderService seckillOrderService;

    /**
     * 确认秒杀订单（占坑后选地址提交）
     */
    @Operation(summary = "确认秒杀订单（占坑后选地址提交，生成正式订单）")
    @PostMapping("/seckill/confirm")
    public CommonResult<Long> confirmSeckillOrder(@RequestBody SeckillConfirmParam param) {
        Long orderId = seckillOrderService.confirmSeckillOrder(param);
        return CommonResult.success(orderId, "下单成功");
    }
}
