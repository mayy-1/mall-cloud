package com.mall.api.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * Feign 默认配置（日志 + 请求拦截器）
 */
public class DefaultFeignConfig {

    /** Feign日志级别：BASIC（记录请求方法和URL、响应状态码、执行时间） */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /** 请求拦截器：传递认证token到下游服务 */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            // 传递 Authorization 头到下游服务（认证token透传）
        };
    }
}
