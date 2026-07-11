package com.mall.member.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.mall.member.model.UmsMember;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员管理Service
 * Created by macro on 2018/8/3.
 */
public interface IMemberService {
    /** 根据用户名获取会员 */
    UmsMember getByUsername(String username);

    /** 根据ID获取会员 */
    UmsMember getById(Long id);

    /** 会员注册 */
    @Transactional
    void register(String username, String password, String telephone, String authCode);

    /** 生成验证码 */
    void generateAuthCode(String telephone);

    /** 修改密码 */
    @Transactional
    void updatePassword(String telephone, String password, String authCode);

    /** 获取当前登录会员 */
    UmsMember getCurrentMember();

    /** 更新会员积分 */
    void updateIntegration(Long id, Integer integration);

    /** 会员登录 */
    SaTokenInfo login(String username, String password);

    /** 会员登出 */
    void logout();
}
