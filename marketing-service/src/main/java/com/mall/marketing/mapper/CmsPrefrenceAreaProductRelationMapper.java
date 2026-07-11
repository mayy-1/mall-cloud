package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsPrefrenceAreaProductRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 优选区域商品关联Mapper
 * 提供优选区域与商品关联关系的增删改查操作
 */
@Primary
public interface CmsPrefrenceAreaProductRelationMapper {

    int deleteByPrimaryKey(Long id);

    int insert(CmsPrefrenceAreaProductRelation row);

    int insertSelective(CmsPrefrenceAreaProductRelation row);
    CmsPrefrenceAreaProductRelation selectByPrimaryKey(Long id);

    List<CmsPrefrenceAreaProductRelation> selectByCondition(CmsPrefrenceAreaProductRelation record);

    int deleteByCondition(CmsPrefrenceAreaProductRelation record);

    int updateSelectiveByCondition(@Param("record") CmsPrefrenceAreaProductRelation record, @Param("condition") CmsPrefrenceAreaProductRelation condition);

    int updateByPrimaryKeySelective(CmsPrefrenceAreaProductRelation row);

    int updateByPrimaryKey(CmsPrefrenceAreaProductRelation row);
}
