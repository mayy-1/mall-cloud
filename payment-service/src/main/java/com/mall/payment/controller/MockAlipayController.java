package com.mall.payment.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.payment.domain.dto.AliPayParam;
import com.mall.payment.service.IAlipayService;
import com.mall.payment.config.AlipayConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 模拟支付 Controller（跳过真实支付宝）
 *
 * 说明：
 * 1. 真实支付宝支付逻辑见 AlipayController（已注释保留），联调时取消注释即可恢复。
 * 2. 本 Controller 保持 /alipay 路径不变，前端与网关均无需改动。
 * 3. 内部调用 MockAlipayServiceImpl（实现 IAlipayService），直接标记订单支付成功。
 */
@RestController
@Tag(name = "MockAlipayController", description = "模拟支付（跳过支付宝）")
@RequestMapping("/alipay")
@RequiredArgsConstructor
public class MockAlipayController {

    /** 支付宝配置（读取 charset、returnUrl 等） */
    private final AlipayConfig alipayConfig;
    /** 模拟支付服务（实现 IAlipayService） */
    private final IAlipayService alipayService;

    @Operation(summary = "模拟电脑网站支付（直接标记支付成功）")
    @GetMapping("/pay")
    public void pay(AliPayParam aliPayParam, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=" + alipayConfig.getCharset());
        response.getWriter().write(alipayService.pay(aliPayParam));
        response.getWriter().flush();
        response.getWriter().close();
    }

    @Operation(summary = "模拟手机网站支付（直接标记支付成功）")
    @GetMapping("/webPay")
    public void webPay(AliPayParam aliPayParam, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=" + alipayConfig.getCharset());
        response.getWriter().write(alipayService.webPay(aliPayParam));
        response.getWriter().flush();
        response.getWriter().close();
    }

    @Operation(summary = "模拟异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request){
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }
        return alipayService.notify(params);
    }

    @Operation(summary = "模拟交易查询（直接返回 TRADE_SUCCESS）")
    @GetMapping("/query")
    public CommonResult<String> query(String outTradeNo, String tradeNo){
        return CommonResult.success(alipayService.query(outTradeNo,tradeNo));
    }
}
