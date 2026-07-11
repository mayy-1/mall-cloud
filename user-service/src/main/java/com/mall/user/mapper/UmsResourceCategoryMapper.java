package com.mall.user.mapper;

import com.mall.user.model.UmsResourceCategory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**资源分类Mapper */
public interface UmsResourceCategoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsResourceCategory row);

    int insertSelective(UmsResourceCategory row);

    UmsResourceCategory selectByPrimaryKey(Long id);

    List<UmsResourceCategory> selectByCondition(UmsResourceCategory record);

    int deleteByCondition(UmsResourceCategory record);

    int updateByPrimaryKeySelective(UmsResourceCategory row);

    int updateByPrimaryKey(UmsResourceCategory row);
}