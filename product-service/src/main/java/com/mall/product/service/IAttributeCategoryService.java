package com.mall.product.service;

import com.mall.product.domain.dto.PmsProductAttributeCategoryItem;
import com.mall.product.model.PmsProductAttributeCategory;

import java.util.List;

/**
 * 商品属性分类Service
 * 【管理端专用】属性分类的增删改查，用于管理端商品属性分类管理
 */
public interface IAttributeCategoryService {
    /**
     * 创建属性分类
     */
    int create(String name);

    /**
     * 修改属性分类
     */
    int update(Long id, String name);

    /**
     * 删除属性分类
     */
    int delete(Long id);

    /**
     * 获取属性分类详情
     */
    PmsProductAttributeCategory getItem(Long id);

    /**
     * 分页查询属性分类
     */
    List<PmsProductAttributeCategory> getList(Integer pageSize, Integer pageNum);

    /**
     * 获取包含属性的属性分类
     */
    List<PmsProductAttributeCategoryItem> getListWithAttr();
}
