package com.mall.api.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 默认配置（日志 + 请求拦截器）
 */
public class DefaultFeignConfig {

    /** Feign日志级别：BASIC（记录请求方法和URL、响应状态码、执行时间） */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /** 请求拦截器：把上游请求头透传到下游服务，保证跨服务调用上下文（登录态）不丢失 */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            // 优先取异步子线程显式设置的 token（线程池并行查询场景，ThreadLocal 不跨线程）
            String token = FeignAuthContext.getToken();
            // 兜底从当前 Servlet 请求上下文取（同步主线程场景）
            if (token == null) {
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes == null) {
                    // 无任何上下文（如 MQ 消费、定时任务触发 Feign）时直接放行
                    return;
                }
                token = attributes.getRequest().getHeader("Authorization");
            }
            // 统一补齐 Bearer 前缀后透传，下游 Sa-Token 用同一 token 校验登录
            if (token != null && !token.isBlank()) {
                if (!token.startsWith("Bearer ")) {
                    token = "Bearer " + token;
                }
                template.header("Authorization", token);
            }
        };
    }
}
