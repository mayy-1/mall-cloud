package com.mall.member.service.impl;

import com.mym.mall.common.annotation.CacheException;
import com.mym.mall.common.service.RedisService;
import com.mall.member.model.UmsMember;
import com.mall.member.service.IMemberCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * UmsMemberCacheService实现类
 * Created by macro on 2020/3/14.
 */
@Service
@RequiredArgsConstructor
public class MemberCacheServiceImpl implements IMemberCacheService {
    /** Redis缓存服务 */
    private final RedisService redisService;
    /** Redis数据库索引 */
    @Value("${redis.database}")
    private String REDIS_DATABASE;
    /** 通用缓存过期时间 */
    @Value("${redis.expire.common}")
    private Long REDIS_EXPIRE;
    /** 验证码过期时间 */
    @Value("${redis.expire.authCode}")
    private Long REDIS_EXPIRE_AUTH_CODE;
    /** 会员缓存Key前缀 */
    @Value("${redis.key.member}")
    private String REDIS_KEY_MEMBER;
    /** 验证码缓存Key前缀 */
    @Value("${redis.key.authCode}")
    private String REDIS_KEY_AUTH_CODE;

    @Override
    public void delMember(Long memberId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_MEMBER + ":" + memberId;
        redisService.del(key);
    }

    @Override
    public UmsMember getMember(Long memberId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_MEMBER + ":" + memberId;
        return (UmsMember) redisService.get(key);
    }

    @Override
    public void setMember(UmsMember member) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_MEMBER + ":" + member.getId();
        redisService.set(key, member, REDIS_EXPIRE);
    }

    @CacheException
    @Override
    public void setAuthCode(String telephone, String authCode) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_AUTH_CODE + ":" + telephone;
        redisService.set(key, authCode, REDIS_EXPIRE_AUTH_CODE);
    }

    @CacheException
    @Override
    public String getAuthCode(String telephone) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_AUTH_CODE + ":" + telephone;
        return (String) redisService.get(key);
    }
}
