package com.mall.api.config;

/**
 * Feign 调用鉴权上下文 —— 跨线程传递登录 token
 */
public final class FeignAuthContext {

    /** 持有当前线程需要透传给下游 Feign 请求的 token */
    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();

    private FeignAuthContext() {
    }

    /** 设置当前线程待透传的 token（异步子线程场景） */
    public static void setToken(String token) {
        TOKEN.set(token);
    }

    /** 获取当前线程待透传的 token，无则返回 null */
    public static String getToken() {
        return TOKEN.get();
    }

    /** 清理当前线程的 token，防止线程复用造成串号 */
    public static void clear() {
        TOKEN.remove();
    }
}
