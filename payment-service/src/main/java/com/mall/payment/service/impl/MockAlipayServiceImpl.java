package com.mall.payment.service.impl;

import com.mall.api.client.order.OrderClient;
import com.mall.payment.config.AlipayConfig;
import com.mall.payment.domain.dto.AliPayParam;
import com.mall.payment.service.IAlipayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 模拟支付 Service 实现（跳过真实支付宝，直接标记订单支付成功）
 *
 * 说明：
 * 1. 真实支付宝支付逻辑见 AlipayServiceImpl（已注释保留），联调时取消注释即可恢复。
 * 2. 本实现不再调用支付宝 SDK，而是直接 Feign 调用 order-service 将订单标记为已支付。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockAlipayServiceImpl implements IAlipayService {

    /** 订单服务 Feign 客户端 */
    private final OrderClient orderClient;
    /** 支付宝配置（仅用于读取 returnUrl 跳转地址） */
    private final AlipayConfig alipayConfig;

    @Override
    public String pay(AliPayParam aliPayParam) {
        return mockPaySuccess(aliPayParam.getOutTradeNo());
    }

    @Override
    public String webPay(AliPayParam aliPayParam) {
        return mockPaySuccess(aliPayParam.getOutTradeNo());
    }

    @Override
    public String notify(Map<String, String> params) {
        String outTradeNo = params.get("out_trade_no");
        if (outTradeNo != null && !outTradeNo.isEmpty()) {
            orderClient.paySuccessByOrderSn(outTradeNo, 1);
            log.info("[模拟支付回调] 订单 {} 已标记为支付成功", outTradeNo);
        }
        return "success";
    }

    @Override
    public String query(String outTradeNo, String tradeNo) {
        if (outTradeNo != null && !outTradeNo.isEmpty()) {
            orderClient.paySuccessByOrderSn(outTradeNo, 1);
            log.info("[模拟支付查询] 订单 {} 已标记为支付成功", outTradeNo);
        }
        return "TRADE_SUCCESS";
    }

    /**
     * 模拟支付成功：直接更新订单状态，并返回跳转支付成功页的 HTML
     */
    private String mockPaySuccess(String outTradeNo) {
        orderClient.paySuccessByOrderSn(outTradeNo, 1);
        log.info("[模拟支付] 订单 {} 已标记为支付成功", outTradeNo);
        return buildSuccessHtml(outTradeNo, alipayConfig.getReturnUrl());
    }

    /**
     * 构建支付成功提示页面（若配置了 returnUrl 则自动跳转）
     */
    private String buildSuccessHtml(String outTradeNo, String returnUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        sb.append("<title>支付成功</title></head><body>");
        sb.append("<div style=\"text-align:center;margin-top:80px;font-family:sans-serif;\">");
        sb.append("<h2>支付成功（模拟）</h2>");
        sb.append("<p>订单号：").append(outTradeNo).append("</p>");
        if (returnUrl != null && !returnUrl.isEmpty()) {
            sb.append("<p>正在返回支付成功页...</p>");
            sb.append("<script>setTimeout(function(){window.location.href='")
              .append(returnUrl).append("';},800);</script>");
        } else {
            sb.append("<p>请返回应用查看订单状态</p>");
        }
        sb.append("</div></body></html>");
        return sb.toString();
    }
}
