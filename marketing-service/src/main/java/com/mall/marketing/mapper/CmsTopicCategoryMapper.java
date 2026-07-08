package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsTopicCategory;
import com.mall.marketing.model.CmsTopicCategoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 话题分类Mapper
 * 提供话题分类的增删改查操作
 */
public interface CmsTopicCategoryMapper {
    long countByExample(CmsTopicCategoryExample example);

    int deleteByExample(CmsTopicCategoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CmsTopicCategory row);

    int insertSelective(CmsTopicCategory row);

    List<CmsTopicCategory> selectByExample(CmsTopicCategoryExample example);

    CmsTopicCategory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") CmsTopicCategory row, @Param("example") CmsTopicCategoryExample example);

    int updateByExample(@Param("row") CmsTopicCategory row, @Param("example") CmsTopicCategoryExample example);

    int updateByPrimaryKeySelective(CmsTopicCategory row);

    int updateByPrimaryKey(CmsTopicCategory row);
}