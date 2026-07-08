package com.mall.trade.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.mall.trade.config.AlipayConfig;
import com.mall.trade.domain.dto.AliPayParam;
import com.mall.trade.service.IAlipayService;
import com.mall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @auther macrozheng
 * @description 支付宝支付Service实现类
 * @date 2023/9/8
 * @github https://github.com/macrozheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayServiceImpl implements IAlipayService {

    /** 支付宝支付配置 */
    private final AlipayConfig alipayConfig;
    /** 支付宝API客户端 */
    private final AlipayClient alipayClient;
    /** 订单服务 */
    private final IOrderService orderService;

    @Override
    public String pay(AliPayParam aliPayParam) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        if(StrUtil.isNotEmpty(alipayConfig.getNotifyUrl())){
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
        }
        if(StrUtil.isNotEmpty(alipayConfig.getReturnUrl())){
            request.setReturnUrl(alipayConfig.getReturnUrl());
        }
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", aliPayParam.getOutTradeNo());
        bizContent.put("total_amount", aliPayParam.getTotalAmount());
        bizContent.put("subject", aliPayParam.getSubject());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());
        String formHtml = null;
        try {
            formHtml = alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return formHtml;
    }

    @Override
    public String notify(Map<String, String> params) {
        String result = "failure";
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset(), alipayConfig.getSignType());
        } catch (AlipayApiException e) {
            log.error("支付回调签名校验异常！",e);
            e.printStackTrace();
        }
        if (signVerified) {
            String tradeStatus = params.get("trade_status");
            if("TRADE_SUCCESS".equals(tradeStatus)){
                result = "success";
                log.info("notify方法被调用了，tradeStatus:{}",tradeStatus);
                String outTradeNo = params.get("out_trade_no");
                orderService.paySuccessByOrderSn(outTradeNo,1);
            }else{
                log.warn("订单未支付成功，trade_status:{}",tradeStatus);
            }
        } else {
            log.warn("支付回调签名校验失败！");
        }
        return result;
    }

    @Override
    public String query(String outTradeNo, String tradeNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        if(StrUtil.isNotEmpty(outTradeNo)){
            bizContent.put("out_trade_no",outTradeNo);
        }
        if(StrUtil.isNotEmpty(tradeNo)){
            bizContent.put("trade_no",tradeNo);
        }
        String[] queryOptions = {"trade_settle_info"};
        bizContent.put("query_options", queryOptions);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.error("查询支付宝账单异常！",e);
        }
        if(response.isSuccess()){
            log.info("查询支付宝账单成功！");
            if("TRADE_SUCCESS".equals(response.getTradeStatus())){
                orderService.paySuccessByOrderSn(outTradeNo,1);
            }
        } else {
            log.error("查询支付宝账单失败！");
        }
        return response.getTradeStatus();
    }

    @Override
    public String webPay(AliPayParam aliPayParam) {
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest ();
        if(StrUtil.isNotEmpty(alipayConfig.getNotifyUrl())){
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
        }
        if(StrUtil.isNotEmpty(alipayConfig.getReturnUrl())){
            request.setReturnUrl(alipayConfig.getReturnUrl());
        }
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", aliPayParam.getOutTradeNo());
        bizContent.put("total_amount", aliPayParam.getTotalAmount());
        bizContent.put("subject", aliPayParam.getSubject());
        bizContent.put("product_code", "QUICK_WAP_WAY");
        request.setBizContent(bizContent.toString());
        String formHtml = null;
        try {
            formHtml = alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return formHtml;
    }
}
