package com.mall.user.service.impl;

import com.mym.mall.common.service.RedisService;
import com.mall.user.model.UmsAdmin;
import com.mall.user.service.UmsAdminCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 后台用户缓存 Service
 */
@Service
@RequiredArgsConstructor
public class UmsAdminCacheServiceImpl implements UmsAdminCacheService {

    /** Redis服务 */
    private final RedisService redisService;

    /** Redis数据库编号 */
    @Value("${redis.database}")
    private String REDIS_DATABASE;
    /** Redis过期时间 */
    @Value("${redis.expire.common}")
    private Long REDIS_EXPIRE;
    /** Redis管理员缓存键前缀 */
    @Value("${redis.key.admin}")
    private String REDIS_KEY_ADMIN;

    @Override
    public void delAdmin(Long adminId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + adminId;
        redisService.del(key);
    }

    @Override
    public UmsAdmin getAdmin(Long adminId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + adminId;
        return (UmsAdmin) redisService.get(key);
    }

    @Override
    public void setAdmin(UmsAdmin admin) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + admin.getId();
        redisService.set(key, admin, REDIS_EXPIRE);
    }
}
