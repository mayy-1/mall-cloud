package com.mall.product.mapper;

import com.mall.product.domain.dto.ProductAttrInfo;
import com.mall.product.model.PmsProductAttribute;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 商品属性Mapper
 * 【管理端专用】商品属性/参数CRUD + getProductAttrInfo(根据分类ID获取属性信息)
 * 对应表: pms_product_attribute
 */
@Primary
public interface PmsProductAttributeMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProductAttribute row);

    int insertSelective(PmsProductAttribute row);
    PmsProductAttribute selectByPrimaryKey(Long id);

    List<PmsProductAttribute> selectByCondition(PmsProductAttribute record);

    int deleteByCondition(PmsProductAttribute record);

    int updateSelectiveByCondition(@Param("record") PmsProductAttribute record, @Param("condition") PmsProductAttribute condition);

    int updateByPrimaryKeySelective(PmsProductAttribute row);

    int updateByPrimaryKey(PmsProductAttribute row);

    /**
     * 根据商品分类ID获取属性及参数信息（管理端商品添加页展示）
     */
    List<ProductAttrInfo> getProductAttrInfo(Long productCategoryId);
}
