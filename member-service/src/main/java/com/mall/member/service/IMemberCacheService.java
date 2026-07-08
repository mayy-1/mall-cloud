package com.mall.member.service;

import com.mall.member.model.UmsMember;

/**
 * 会员信息缓存业务类
 * Created by macro on 2020/3/14.
 */
public interface IMemberCacheService {
    /** 删除会员缓存 */
    void delMember(Long memberId);

    /** 获取会员缓存 */
    UmsMember getMember(Long memberId);

    /** 设置会员缓存 */
    void setMember(UmsMember member);

    /** 设置验证码缓存 */
    void setAuthCode(String telephone, String authCode);

    /** 获取验证码缓存 */
    String getAuthCode(String telephone);
}
