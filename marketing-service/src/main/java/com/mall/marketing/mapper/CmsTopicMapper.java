package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsTopic;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 话题Mapper
 * 提供话题的增删改查操作
 */
public interface CmsTopicMapper {

    int deleteByPrimaryKey(Long id);

    int insert(CmsTopic row);

    int insertSelective(CmsTopic row);
    CmsTopic selectByPrimaryKey(Long id);

    List<CmsTopic> selectByCondition(CmsTopic record);

    int deleteByCondition(CmsTopic record);

    int updateSelectiveByCondition(@Param("record") CmsTopic record, @Param("condition") CmsTopic condition);

    int updateByPrimaryKeySelective(CmsTopic row);

    int updateByPrimaryKeyWithBLOBs(CmsTopic row);

    int updateByPrimaryKey(CmsTopic row);
}