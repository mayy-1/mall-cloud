package com.mall.trade.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.trade.domain.dto.ConfirmOrderResult;
import com.mall.trade.domain.dto.OmsOrderDetail;
import com.mall.trade.domain.dto.OrderParam;
import com.mall.trade.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单管理Controller
 * Created by macro on 2018/8/30.
 */
@RestController
@Tag(name = "OrderController", description = "订单管理")
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    /** 订单业务服务 */
    private final IOrderService orderService;

    @Operation(summary = "根据购物车信息生成确认单信息")
    @PostMapping("/generateConfirmOrder")
    public CommonResult<ConfirmOrderResult> generateConfirmOrder(@RequestBody List<Long> cartIds) {
        ConfirmOrderResult confirmOrderResult = orderService.generateConfirmOrder(cartIds);
        return CommonResult.success(confirmOrderResult);
    }

    @Operation(summary = "根据购物车信息生成订单")
    @PostMapping("/generateOrder")
    public CommonResult generateOrder(@RequestBody OrderParam orderParam) {
        Map<String, Object> result = orderService.generateOrder(orderParam);
        return CommonResult.success(result, "下单成功");
    }

    @Operation(summary = "用户支付成功的回调")
    @PostMapping("/paySuccess")
    public CommonResult paySuccess(@RequestParam Long orderId, @RequestParam Integer payType) {
        Integer count = orderService.paySuccess(orderId, payType);
        return CommonResult.success(count, "支付成功");
    }

    @Operation(summary = "自动取消超时订单")
    @PostMapping("/cancelTimeOutOrder")
    public CommonResult cancelTimeOutOrder() {
        orderService.cancelTimeOutOrder();
        return CommonResult.success(null);
    }

    @Operation(summary = "取消单个超时订单")
    @PostMapping("/cancelOrder")
    public CommonResult cancelOrder(Long orderId) {
        orderService.sendDelayMessageCancelOrder(orderId);
        return CommonResult.success(null);
    }

    @Operation(summary = "按状态分页获取用户订单列表")
    @Parameter(name = "status", description = "订单状态：-1->全部；0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭",
            in = ParameterIn.QUERY, schema = @Schema(type = "integer",defaultValue = "-1",allowableValues = {"-1","0","1","2","3","4"}))
    @GetMapping("/list")
    public CommonResult<CommonPage<OmsOrderDetail>> list(@RequestParam Integer status,
                                                   @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                                   @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        CommonPage<OmsOrderDetail> orderPage = orderService.list(status, pageNum, pageSize);
        return CommonResult.success(orderPage);
    }

    @Operation(summary = "根据ID获取订单详情")
    @GetMapping("/detail/{orderId}")
    public CommonResult<OmsOrderDetail> detail(@PathVariable Long orderId) {
        OmsOrderDetail orderDetail = orderService.detail(orderId);
        return CommonResult.success(orderDetail);
    }

    @Operation(summary = "用户取消订单")
    @PostMapping("/cancelUserOrder")
    public CommonResult cancelUserOrder(Long orderId) {
        orderService.cancelOrder(orderId);
        return CommonResult.success(null);
    }

    @Operation(summary = "用户确认收货")
    @PostMapping("/confirmReceiveOrder")
    public CommonResult confirmReceiveOrder(Long orderId) {
        orderService.confirmReceiveOrder(orderId);
        return CommonResult.success(null);
    }

    @Operation(summary = "用户删除订单")
    @PostMapping("/deleteOrder")
    public CommonResult deleteOrder(Long orderId) {
        orderService.deleteOrder(orderId);
        return CommonResult.success(null);
    }
}
