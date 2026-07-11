package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsSubjectProductRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 专题商品关联Mapper
 * 提供专题与商品关联关系的增删改查操作
 */
@Primary
public interface CmsSubjectProductRelationMapper {

    int deleteByPrimaryKey(Long id);

    int insert(CmsSubjectProductRelation row);

    int insertSelective(CmsSubjectProductRelation row);
    CmsSubjectProductRelation selectByPrimaryKey(Long id);

    List<CmsSubjectProductRelation> selectByCondition(CmsSubjectProductRelation record);

    int deleteByCondition(CmsSubjectProductRelation record);

    int updateSelectiveByCondition(@Param("record") CmsSubjectProductRelation record, @Param("condition") CmsSubjectProductRelation condition);

    int updateByPrimaryKeySelective(CmsSubjectProductRelation row);

    int updateByPrimaryKey(CmsSubjectProductRelation row);
}
