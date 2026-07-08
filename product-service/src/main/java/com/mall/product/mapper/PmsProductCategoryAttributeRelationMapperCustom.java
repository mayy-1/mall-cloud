package com.mall.product.mapper;

import com.mall.product.model.PmsProductCategoryAttributeRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义商品分类和属性关系Mapper
 * Created by macro on 2018/5/23.
 */
public interface PmsProductCategoryAttributeRelationMapperCustom extends PmsProductCategoryAttributeRelationMapper {
    /**
     * 批量创建
     */
    int insertList(@Param("list") List<PmsProductCategoryAttributeRelation> productCategoryAttributeRelationList);
}
