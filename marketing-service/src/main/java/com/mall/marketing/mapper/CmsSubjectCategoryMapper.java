package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsSubjectCategory;
import com.mall.marketing.model.CmsSubjectCategoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 专题分类Mapper
 * 提供专题分类的增删改查操作
 */
public interface CmsSubjectCategoryMapper {
    long countByExample(CmsSubjectCategoryExample example);

    int deleteByExample(CmsSubjectCategoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CmsSubjectCategory row);

    int insertSelective(CmsSubjectCategory row);

    List<CmsSubjectCategory> selectByExample(CmsSubjectCategoryExample example);

    CmsSubjectCategory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") CmsSubjectCategory row, @Param("example") CmsSubjectCategoryExample example);

    int updateByExample(@Param("row") CmsSubjectCategory row, @Param("example") CmsSubjectCategoryExample example);

    int updateByPrimaryKeySelective(CmsSubjectCategory row);

    int updateByPrimaryKey(CmsSubjectCategory row);
}