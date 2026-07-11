package com.mall.user.mapper;

import com.mall.user.model.UmsAdminLoginLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 后台用户登录日志Mapper
 */
public interface UmsAdminLoginLogMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsAdminLoginLog row);

    int insertSelective(UmsAdminLoginLog row);

    UmsAdminLoginLog selectByPrimaryKey(Long id);

    List<UmsAdminLoginLog> selectByCondition(UmsAdminLoginLog record);

    int deleteByCondition(UmsAdminLoginLog record);

    int updateByPrimaryKeySelective(UmsAdminLoginLog row);

    int updateByPrimaryKey(UmsAdminLoginLog row);
}