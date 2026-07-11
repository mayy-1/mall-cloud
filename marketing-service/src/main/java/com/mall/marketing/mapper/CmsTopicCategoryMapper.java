package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsTopicCategory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 话题分类Mapper
 * 提供话题分类的增删改查操作
 */
public interface CmsTopicCategoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(CmsTopicCategory row);

    int insertSelective(CmsTopicCategory row);
    CmsTopicCategory selectByPrimaryKey(Long id);

    List<CmsTopicCategory> selectByCondition(CmsTopicCategory record);

    int deleteByCondition(CmsTopicCategory record);

    int updateSelectiveByCondition(@Param("record") CmsTopicCategory record, @Param("condition") CmsTopicCategory condition);

    int updateByPrimaryKeySelective(CmsTopicCategory row);

    int updateByPrimaryKey(CmsTopicCategory row);
}