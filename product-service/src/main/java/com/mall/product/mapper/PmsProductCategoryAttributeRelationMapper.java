package com.mall.product.mapper;

import com.mall.product.model.PmsProductCategoryAttributeRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**商品分类属性关系Mapper */
@Primary
public interface PmsProductCategoryAttributeRelationMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProductCategoryAttributeRelation row);

    int insertSelective(PmsProductCategoryAttributeRelation row);
    PmsProductCategoryAttributeRelation selectByPrimaryKey(Long id);

    List<PmsProductCategoryAttributeRelation> selectByCondition(PmsProductCategoryAttributeRelation record);

    int deleteByCondition(PmsProductCategoryAttributeRelation record);

    int updateSelectiveByCondition(@Param("record") PmsProductCategoryAttributeRelation record, @Param("condition") PmsProductCategoryAttributeRelation condition);

    int updateByPrimaryKeySelective(PmsProductCategoryAttributeRelation row);

    int updateByPrimaryKey(PmsProductCategoryAttributeRelation row);
}
