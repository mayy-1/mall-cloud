package com.mym.mall.config;

import com.mym.mall.common.config.BaseRedisConfig;

/**
 * Redis 配置适配。
 * BaseRedisConfig（mall-common）已通过 @Configuration 自动注册 redisTemplate 等 bean，
 * 本类仅作为 gateway 模块的占位适配，不可再声明 @Configuration 避免 bean 重复定义。
 */
public class RedisConfig extends BaseRedisConfig {
}
