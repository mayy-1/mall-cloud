package com.mall.product.mapper;

import com.mall.product.model.CmsSubjectProductRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**专题商品关系Mapper */
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

    /**
     * 批量插入专题商品关系
     */
    int insertList(@Param("list") List<CmsSubjectProductRelation> list);
}
