package com.mall.product.mapper;

import com.mall.product.domain.dto.PmsProductAttributeCategoryItem;

import java.util.List;

/**
 * 自定义商品属性分类Mapper
 * Created by macro on 2018/5/24.
 */
public interface PmsProductAttributeCategoryMapperCustom extends PmsProductAttributeCategoryMapper {
    /**
     * 获取包含属性的商品属性分类
     */
    List<PmsProductAttributeCategoryItem> getListWithAttr();
}
