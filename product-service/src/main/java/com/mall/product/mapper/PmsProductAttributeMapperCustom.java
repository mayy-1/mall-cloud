package com.mall.product.mapper;

import com.mall.product.domain.dto.ProductAttrInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义商品属性Mapper
 * Created by macro on 2018/5/23.
 */
public interface PmsProductAttributeMapperCustom extends PmsProductAttributeMapper {
    /**
     * 获取商品属性信息
     */
    List<ProductAttrInfo> getProductAttrInfo(@Param("id") Long productCategoryId);
}
