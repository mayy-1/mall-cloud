package com.mall.product.mapper;

import com.mall.product.domain.dto.PmsProductCategoryWithChildrenItem;

import java.util.List;

/**
 * 商品分类自定义Mapper
 * Created by macro on 2018/5/25.
 */
public interface PmsProductCategoryMapperCustom extends PmsProductCategoryMapper {
    /**
     * 获取商品分类及其子分类
     */
    List<PmsProductCategoryWithChildrenItem> listWithChildren();
}
