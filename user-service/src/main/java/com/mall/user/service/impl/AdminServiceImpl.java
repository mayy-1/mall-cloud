package com.mall.user.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.github.pagehelper.PageHelper;
import com.mym.mall.common.constant.AuthConstant;
import com.mym.mall.common.dto.UserDto;
import com.mym.mall.common.exception.Asserts;
import com.mall.user.mapper.UmsAdminRoleRelationMapperCustom;
import com.mall.user.domain.dto.AdminSaveDTO;
import com.mall.user.domain.dto.UpdatePasswordDTO;
import com.mall.user.mapper.UmsAdminLoginLogMapper;
import com.mall.user.mapper.UmsAdminMapper;
import com.mall.user.model.UmsAdmin;
import com.mall.user.model.UmsAdminExample;
import com.mall.user.model.UmsAdminLoginLog;
import com.mall.user.model.UmsAdminRoleRelation;
import com.mall.user.model.UmsAdminRoleRelationExample;
import com.mall.user.model.UmsResource;
import com.mall.user.model.UmsRole;
import com.mall.user.service.IAdminService;
import com.mall.user.service.UmsAdminCacheService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 后台管理员 Service 实现（黑马商城风格：@RequiredArgsConstructor 注入）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    /** 管理员Mapper */
    private final UmsAdminMapper adminMapper;
    /** 管理员角色关系自定义Mapper */
    private final UmsAdminRoleRelationMapperCustom adminRoleRelationMapper;
    /** 登录日志Mapper */
    private final UmsAdminLoginLogMapper loginLogMapper;
    /** 管理员缓存服务 */
    private final UmsAdminCacheService adminCacheService;

    /** 根据用户名查询管理员 */
    @Override
    public UmsAdmin getAdminByUsername(String username) {
        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<UmsAdmin> adminList = adminMapper.selectByExample(example);
        if (adminList != null && !adminList.isEmpty()) {
            return adminList.get(0);
        }
        return null;
    }

    @Override
    public UmsAdmin register(AdminSaveDTO dto) {
        UmsAdmin umsAdmin = new UmsAdmin();
        BeanUtils.copyProperties(dto, umsAdmin);
        umsAdmin.setCreateTime(new Date());
        umsAdmin.setStatus(1);
        // 检查用户名是否已存在
        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(umsAdmin.getUsername());
        List<UmsAdmin> umsAdminList = adminMapper.selectByExample(example);
        if (!umsAdminList.isEmpty()) {
            return null;
        }
        // BCrypt 加密密码
        String encodePassword = BCrypt.hashpw(umsAdmin.getPassword());
        umsAdmin.setPassword(encodePassword);
        adminMapper.insert(umsAdmin);
        return umsAdmin;
    }

    @Override
    public SaTokenInfo login(String username, String password) {
        if (StrUtil.isEmpty(username) || StrUtil.isEmpty(password)) {
            Asserts.fail("用户名或密码不能为空！");
        }
        UmsAdmin admin = getAdminByUsername(username);
        if (admin == null) {
            Asserts.fail("找不到该用户！");
        }
        if (!BCrypt.checkpw(password, admin.getPassword())) {
            Asserts.fail("密码不正确！");
        }
        if (admin.getStatus() != 1) {
            Asserts.fail("该账号已被禁用！");
        }
        // Sa-Token 登录
        StpUtil.login(admin.getId());
        UserDto userDto = new UserDto();
        userDto.setId(admin.getId());
        userDto.setUsername(admin.getUsername());
        userDto.setClientId(AuthConstant.ADMIN_CLIENT_ID);
        List<UmsResource> resourceList = getResourceList(admin.getId());
        List<String> permissionList = resourceList.stream()
                .map(item -> item.getId() + ":" + item.getName())
                .toList();
        userDto.setPermissionList(permissionList);
        // 用户信息存入 Session
        StpUtil.getSession().set(AuthConstant.STP_ADMIN_INFO, userDto);
        SaTokenInfo saTokenInfo = StpUtil.getTokenInfo();
        insertLoginLog(admin);
        return saTokenInfo;
    }

    /** 插入登录日志 */
    private void insertLoginLog(UmsAdmin admin) {
        if (admin == null) return;
        UmsAdminLoginLog loginLog = new UmsAdminLoginLog();
        loginLog.setAdminId(admin.getId());
        loginLog.setCreateTime(new Date());
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            loginLog.setIp(request.getRemoteAddr());
        }
        loginLogMapper.insert(loginLog);
    }

    @Override
    public UmsAdmin getItem(Long id) {
        return adminMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        UmsAdminExample example = new UmsAdminExample();
        UmsAdminExample.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(keyword)) {
            criteria.andUsernameLike("%" + keyword + "%");
            example.or(example.createCriteria().andNickNameLike("%" + keyword + "%"));
        }
        return adminMapper.selectByExample(example);
    }

    @Override
    public int update(Long id, UmsAdmin admin) {
        admin.setId(id);
        UmsAdmin rawAdmin = adminMapper.selectByPrimaryKey(id);
        if (rawAdmin.getPassword().equals(admin.getPassword())) {
            admin.setPassword(null);
        } else {
            if (StrUtil.isEmpty(admin.getPassword())) {
                admin.setPassword(null);
            } else {
                admin.setPassword(BCrypt.hashpw(admin.getPassword()));
            }
        }
        int count = adminMapper.updateByPrimaryKeySelective(admin);
        adminCacheService.delAdmin(id);
        return count;
    }

    @Override
    public int delete(Long id) {
        int count = adminMapper.deleteByPrimaryKey(id);
        adminCacheService.delAdmin(id);
        return count;
    }

    @Override
    public int updateRole(Long adminId, List<Long> roleIds) {
        int count = roleIds == null ? 0 : roleIds.size();
        // 删除旧关系
        UmsAdminRoleRelationExample adminRoleRelationExample = new UmsAdminRoleRelationExample();
        adminRoleRelationExample.createCriteria().andAdminIdEqualTo(adminId);
        adminRoleRelationMapper.deleteByExample(adminRoleRelationExample);
        // 建立新关系
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<UmsAdminRoleRelation> list = new ArrayList<>();
            for (Long roleId : roleIds) {
                UmsAdminRoleRelation roleRelation = new UmsAdminRoleRelation();
                roleRelation.setAdminId(adminId);
                roleRelation.setRoleId(roleId);
                list.add(roleRelation);
            }
            adminRoleRelationMapper.insertList(list);
        }
        return count;
    }

    @Override
    public List<UmsRole> getRoleList(Long adminId) {
        return adminRoleRelationMapper.getRoleList(adminId);
    }

    @Override
    public List<UmsResource> getResourceList(Long adminId) {
        return adminRoleRelationMapper.getResourceList(adminId);
    }

    @Override
    public int updatePassword(UpdatePasswordDTO dto) {
        if (StrUtil.isEmpty(dto.getUsername())
                || StrUtil.isEmpty(dto.getOldPassword())
                || StrUtil.isEmpty(dto.getNewPassword())) {
            return -1;
        }
        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(dto.getUsername());
        List<UmsAdmin> adminList = adminMapper.selectByExample(example);
        if (CollUtil.isEmpty(adminList)) {
            return -2;
        }
        UmsAdmin umsAdmin = adminList.get(0);
        if (!BCrypt.checkpw(dto.getOldPassword(), umsAdmin.getPassword())) {
            return -3;
        }
        umsAdmin.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        adminMapper.updateByPrimaryKey(umsAdmin);
        adminCacheService.delAdmin(umsAdmin.getId());
        return 1;
    }

    @Override
    public UmsAdmin getCurrentAdmin() {
        UserDto userDto = (UserDto) StpUtil.getSession().get(AuthConstant.STP_ADMIN_INFO);
        UmsAdmin admin = adminCacheService.getAdmin(userDto.getId());
        if (admin == null) {
            admin = adminMapper.selectByPrimaryKey(userDto.getId());
            adminCacheService.setAdmin(admin);
        }
        return admin;
    }

    @Override
    public void logout() {
        UserDto userDto = (UserDto) StpUtil.getSession().get(AuthConstant.STP_ADMIN_INFO);
        adminCacheService.delAdmin(userDto.getId());
        StpUtil.logout();
    }
}
