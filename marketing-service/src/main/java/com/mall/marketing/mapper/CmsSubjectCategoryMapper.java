package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsSubjectCategory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 专题分类Mapper
 * 提供专题分类的增删改查操作
 */
public interface CmsSubjectCategoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(CmsSubjectCategory row);

    int insertSelective(CmsSubjectCategory row);
    CmsSubjectCategory selectByPrimaryKey(Long id);

    List<CmsSubjectCategory> selectByCondition(CmsSubjectCategory record);

    int deleteByCondition(CmsSubjectCategory record);

    int updateSelectiveByCondition(@Param("record") CmsSubjectCategory record, @Param("condition") CmsSubjectCategory condition);

    int updateByPrimaryKeySelective(CmsSubjectCategory row);

    int updateByPrimaryKey(CmsSubjectCategory row);
}