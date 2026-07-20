package com.mall.product.mapper;

import com.mall.product.domain.dto.PmsProductAttributeCategoryItem;
import com.mall.product.model.PmsProductAttributeCategory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 商品属性分类Mapper
 * 【管理端专用】属性分类CRUD + getListWithAttr(含子属性的分类列表)
 * 对应表: pms_product_attribute_category
 */
@Primary
public interface PmsProductAttributeCategoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProductAttributeCategory row);

    int insertSelective(PmsProductAttributeCategory row);

    PmsProductAttributeCategory selectByPrimaryKey(Long id);

    List<PmsProductAttributeCategory> selectByCondition(PmsProductAttributeCategory record);

    int deleteByCondition(PmsProductAttributeCategory record);

    int updateSelectiveByCondition(@Param("record") PmsProductAttributeCategory record, @Param("condition") PmsProductAttributeCategory condition);

    int updateByPrimaryKeySelective(PmsProductAttributeCategory row);

    int updateByPrimaryKey(PmsProductAttributeCategory row);

    /**
     * 获取包含属性列表的分类（管理端分类管理页展示）
     */
    List<PmsProductAttributeCategoryItem> getListWithAttr();
}