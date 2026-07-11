package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsHelpCategory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 帮助分类Mapper
 * 提供帮助分类的增删改查操作
 */
public interface CmsHelpCategoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(CmsHelpCategory row);

    int insertSelective(CmsHelpCategory row);
    CmsHelpCategory selectByPrimaryKey(Long id);

    List<CmsHelpCategory> selectByCondition(CmsHelpCategory record);

    int deleteByCondition(CmsHelpCategory record);

    int updateSelectiveByCondition(@Param("record") CmsHelpCategory record, @Param("condition") CmsHelpCategory condition);

    int updateByPrimaryKeySelective(CmsHelpCategory row);

    int updateByPrimaryKey(CmsHelpCategory row);
}