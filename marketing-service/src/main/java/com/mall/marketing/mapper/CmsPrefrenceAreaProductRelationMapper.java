package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsPrefrenceAreaProductRelation;
import com.mall.marketing.model.CmsPrefrenceAreaProductRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 优选区域商品关联Mapper
 * 提供优选区域与商品关联关系的增删改查操作
 */
@Primary
public interface CmsPrefrenceAreaProductRelationMapper {
    long countByExample(CmsPrefrenceAreaProductRelationExample example);

    int deleteByExample(CmsPrefrenceAreaProductRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CmsPrefrenceAreaProductRelation row);

    int insertSelective(CmsPrefrenceAreaProductRelation row);

    List<CmsPrefrenceAreaProductRelation> selectByExample(CmsPrefrenceAreaProductRelationExample example);

    CmsPrefrenceAreaProductRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") CmsPrefrenceAreaProductRelation row, @Param("example") CmsPrefrenceAreaProductRelationExample example);

    int updateByExample(@Param("row") CmsPrefrenceAreaProductRelation row, @Param("example") CmsPrefrenceAreaProductRelationExample example);

    int updateByPrimaryKeySelective(CmsPrefrenceAreaProductRelation row);

    int updateByPrimaryKey(CmsPrefrenceAreaProductRelation row);
}
