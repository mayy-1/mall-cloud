package com.mall.payment.service;

import com.mall.payment.domain.dto.AliPayParam;

import java.util.Map;

/**
 * @auther macrozheng
 * @description 支付宝支付Service
 * @date 2023/9/8
 * @github https://github.com/macrozheng
 */
public interface IAlipayService {
    /** 支付宝电脑网站支付 */
    String pay(AliPayParam aliPayParam);

    /** 支付宝异步回调处理 */
    String notify(Map<String, String> params);

    /** 支付宝交易查询 */
    String query(String outTradeNo, String tradeNo);

    /** 支付宝手机网站支付 */
    String webPay(AliPayParam aliPayParam);
}

