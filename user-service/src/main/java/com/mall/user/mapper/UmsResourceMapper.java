package com.mall.user.mapper;

import com.mall.user.model.UmsResource;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**后台资源Mapper */
public interface UmsResourceMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsResource row);

    int insertSelective(UmsResource row);

    UmsResource selectByPrimaryKey(Long id);

    List<UmsResource> selectByCondition(UmsResource record);

    int deleteByCondition(UmsResource record);

    int updateByPrimaryKeySelective(UmsResource row);

    int updateByPrimaryKey(UmsResource row);
}