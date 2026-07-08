package com.mall.user.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.mall.user.domain.dto.AdminSaveDTO;
import com.mall.user.domain.dto.UpdatePasswordDTO;
import com.mall.user.model.UmsAdmin;
import com.mall.user.model.UmsResource;
import com.mall.user.model.UmsRole;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 后台管理员 Service 接口（黑马商城风格：I 前缀）
 */
public interface IAdminService {

    /** 根据用户名获取管理员 */
    UmsAdmin getAdminByUsername(String username);

    /** 注册管理员 */
    UmsAdmin register(AdminSaveDTO dto);

    /** 管理员登录 */
    SaTokenInfo login(String username, String password);

    /** 根据ID获取管理员详情 */
    UmsAdmin getItem(Long id);

    /** 分页查询管理员列表 */
    List<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum);

    /** 修改管理员信息 */
    int update(Long id, UmsAdmin admin);

    /** 删除管理员 */
    int delete(Long id);

    /** 给管理员分配角色 */
    @Transactional
    int updateRole(Long adminId, List<Long> roleIds);

    /** 获取管理员的角色列表 */
    List<UmsRole> getRoleList(Long adminId);

    /** 获取管理员的资源列表 */
    List<UmsResource> getResourceList(Long adminId);

    /** 修改管理员密码 */
    int updatePassword(UpdatePasswordDTO dto);

    /** 获取当前登录管理员 */
    UmsAdmin getCurrentAdmin();

    /** 登出 */
    void logout();
}
