package com.mall.product.mapper;

import com.mall.product.model.PmsProductCategoryAttributeRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 商品分类-属性关系Mapper
 * 【管理端专用】管理分类与筛选属性的关联关系，创建/编辑分类时批量插入
 * 对应表: pms_product_category_attribute_relation
 */
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

    /**
     * 批量插入分类-属性关系
     */
    int insertList(@Param("list") List<PmsProductCategoryAttributeRelation> list);
}
