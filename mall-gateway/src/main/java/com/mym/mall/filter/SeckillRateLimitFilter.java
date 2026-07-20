package com.mym.mall.filter;

import com.mym.mall.common.api.CommonResult;
import com.mym.mall.common.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 秒杀接口限流过滤器
 * 基于 Redis 滑动窗口计数器实现，按 userId 和 IP 双重限流
 *
 * 限流策略：
 * - 用户维度：每个用户每秒最多 N 次秒杀请求（默认 5 次/秒）
 * - IP 维度：每个 IP 每秒最多 M 次秒杀请求（默认 10 次/秒）
 * - 超量直接返回 HTTP 429 Too Many Requests，不转发业务服务
 */
@Component
public class SeckillRateLimitFilter implements GlobalFilter, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillRateLimitFilter.class);

    private static final String SECKILL_PATH = "/seckill/execute";
    private static final String RATE_LIMIT_USER_KEY = "seckill:rate:user:%s";
    private static final String RATE_LIMIT_IP_KEY = "seckill:rate:ip:%s";
    private static final int USER_RATE_LIMIT = 5;   // 每用户每秒最多 5 次
    private static final int IP_RATE_LIMIT = 10;    // 每 IP 每秒最多 10 次
    private static final int RATE_WINDOW_SECONDS = 1;

    private final RedisService redisService;

    public SeckillRateLimitFilter(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 仅对秒杀执行接口进行限流
        if (!path.contains(SECKILL_PATH)) {
            return chain.filter(exchange);
        }

        // 1. 获取用户ID（从 Header 或请求参数）
        String userId = getUserId(request);

        // 2. 获取客户端 IP
        String clientIp = getClientIp(request);

        // 3. 用户维度限流
        if (userId != null && !userId.isEmpty()) {
            if (!tryAcquire(String.format(RATE_LIMIT_USER_KEY, userId), USER_RATE_LIMIT)) {
                LOGGER.warn("用户秒杀限流触发, userId={}, ip={}", userId, clientIp);
                return rateLimitResponse(exchange, "请求过于频繁，请稍后再试");
            }
        }

        // 4. IP 维度限流
        if (clientIp != null && !clientIp.isEmpty()) {
            if (!tryAcquire(String.format(RATE_LIMIT_IP_KEY, clientIp), IP_RATE_LIMIT)) {
                LOGGER.warn("IP秒杀限流触发, ip={}", clientIp);
                return rateLimitResponse(exchange, "请求过于频繁，请稍后再试");
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 在鉴权过滤器之后执行，确保 userId 已解析
        return -10;
    }

    /**
     * 滑动窗口计数器限流
     * 使用 Redis INCR + EXPIRE 实现固定窗口计数
     *
     * @param key   Redis 限流 key
     * @param limit 窗口内允许的最大请求数
     * @return true=放行, false=限流
     */
    private boolean tryAcquire(String key, int limit) {
        try {
            Object countObj = redisService.get(key);
            long count = countObj == null ? 0 : Long.parseLong(countObj.toString());

            if (count >= limit) {
                return false;
            }

            // INCR 并在首次设置过期时间
            Long newCount = redisService.incr(key, 1);
            if (newCount != null && newCount == 1) {
                redisService.expire(key, RATE_WINDOW_SECONDS);
            }
            return true;
        } catch (Exception e) {
            // Redis 故障时降级放行，避免影响正常业务
            LOGGER.error("限流Redis操作异常, key={}", key, e);
            return true;
        }
    }

    /**
     * 返回限流响应（HTTP 429）
     */
    private Mono<Void> rateLimitResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().set("Retry-After", String.valueOf(RATE_WINDOW_SECONDS));

        String body = "{\"code\":429,\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 从请求中提取用户ID
     * 优先从 Header 的 memberId 中获取
     */
    private String getUserId(ServerHttpRequest request) {
        // 尝试从 Header 获取
        String memberId = request.getHeaders().getFirst("memberId");
        if (memberId != null && !memberId.isEmpty()) {
            return memberId;
        }
        // 尝试从 Authorization Bearer Token 中解析（Sa-Token 格式）
        // 简化处理：从请求参数获取
        return request.getQueryParams().getFirst("memberId");
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        // 优先从代理头获取
        String ip = headers.getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null
                    ? request.getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        }
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}