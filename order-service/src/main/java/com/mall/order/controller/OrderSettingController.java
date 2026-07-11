package com.mall.order.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.order.model.OmsOrderSetting;
import com.mall.order.service.IOrderSettingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单设置Controller
 * Created by macro on 2018/10/16.
 */
@RestController
@Tag(name = "OrderSettingController", description = "订单设置管理")
@RequestMapping("/orderSetting")
@RequiredArgsConstructor
public class OrderSettingController {
    /** 订单设置服务 */
    private final IOrderSettingService orderSettingService;

    @Operation(summary = "获取指定订单设置")
    @GetMapping("/{id}")
    public CommonResult<OmsOrderSetting> getItem(@PathVariable Long id) {
        OmsOrderSetting orderSetting = orderSettingService.getItem(id);
        return CommonResult.success(orderSetting);
    }

    @Operation(summary = "修改指定订单设置")
    @PostMapping("/update/{id}")
    public CommonResult update(@PathVariable Long id, @RequestBody OmsOrderSetting orderSetting) {
        int count = orderSettingService.update(id,orderSetting);
        if(count>0){
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
