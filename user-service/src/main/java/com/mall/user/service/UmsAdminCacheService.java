package com.mall.user.service;

import com.mall.user.model.UmsAdmin;

/**
 * 后台用户缓存 Service
 */
public interface UmsAdminCacheService {

    UmsAdmin getAdmin(Long adminId);

    void setAdmin(UmsAdmin admin);

    void delAdmin(Long adminId);
}
