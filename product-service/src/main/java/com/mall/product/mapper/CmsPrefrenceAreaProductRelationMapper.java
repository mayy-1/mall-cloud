package com.mall.product.mapper;

import com.mall.product.model.CmsPrefrenceAreaProductRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**优选专区商品关系Mapper */
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
