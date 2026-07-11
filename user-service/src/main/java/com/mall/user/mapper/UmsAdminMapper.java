package com.mall.user.mapper;

import com.mall.user.model.UmsAdmin;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 后台用户Mapper
 * 提供UmsAdmin的CRUD操作
 */
public interface UmsAdminMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsAdmin row);

    int insertSelective(UmsAdmin row);

    UmsAdmin selectByPrimaryKey(Long id);

    List<UmsAdmin> selectByCondition(UmsAdmin record);

    int deleteByCondition(UmsAdmin record);

    int updateByPrimaryKeySelective(UmsAdmin row);

    int updateByPrimaryKey(UmsAdmin row);

}