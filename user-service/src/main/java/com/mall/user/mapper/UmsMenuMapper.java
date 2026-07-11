package com.mall.user.mapper;

import com.mall.user.model.UmsMenu;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 后台菜单Mapper
 */
public interface UmsMenuMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsMenu row);

    int insertSelective(UmsMenu row);

    UmsMenu selectByPrimaryKey(Long id);

    List<UmsMenu> selectByCondition(UmsMenu record);

    int deleteByCondition(UmsMenu record);

    int updateByPrimaryKeySelective(UmsMenu row);

    int updateByPrimaryKey(UmsMenu row);
}