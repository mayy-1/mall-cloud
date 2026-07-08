package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsSubjectProductRelation;
import com.mall.marketing.model.CmsSubjectProductRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 专题商品关联Mapper
 * 提供专题与商品关联关系的增删改查操作
 */
@Primary
public interface CmsSubjectProductRelationMapper {
    long countByExample(CmsSubjectProductRelationExample example);

    int deleteByExample(CmsSubjectProductRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CmsSubjectProductRelation row);

    int insertSelective(CmsSubjectProductRelation row);

    List<CmsSubjectProductRelation> selectByExample(CmsSubjectProductRelationExample example);

    CmsSubjectProductRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") CmsSubjectProductRelation row, @Param("example") CmsSubjectProductRelationExample example);

    int updateByExample(@Param("row") CmsSubjectProductRelation row, @Param("example") CmsSubjectProductRelationExample example);

    int updateByPrimaryKeySelective(CmsSubjectProductRelation row);

    int updateByPrimaryKey(CmsSubjectProductRelation row);
}
