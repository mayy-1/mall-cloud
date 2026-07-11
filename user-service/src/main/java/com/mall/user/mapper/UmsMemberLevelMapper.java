package com.mall.user.mapper;

import com.mall.user.model.UmsMemberLevel;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员等级Mapper
 */
public interface UmsMemberLevelMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberLevel row);

    int insertSelective(UmsMemberLevel row);

    UmsMemberLevel selectByPrimaryKey(Long id);

    List<UmsMemberLevel> selectByCondition(UmsMemberLevel record);

    int deleteByCondition(UmsMemberLevel record);

    int updateByPrimaryKeySelective(UmsMemberLevel row);

    int updateByPrimaryKey(UmsMemberLevel row);
}
