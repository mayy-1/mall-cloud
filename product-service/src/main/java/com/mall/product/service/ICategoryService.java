package com.mall.product.service;

import com.mall.product.domain.dto.PmsProductCategoryParam;
import com.mall.product.domain.dto.PmsProductCategoryWithChildrenItem;
import com.mall.product.model.PmsProductCategory;

import java.util.List;

/**
 * 商品分类Service
 * 分类增删改、导航/显示状态切换为管理端专用
 */
public interface ICategoryService {

    int create(PmsProductCategoryParam pmsProductCategoryParam);

    int update(Long id, PmsProductCategoryParam pmsProductCategoryParam);

    List<PmsProductCategory> getList(Long parentId, Integer pageSize, Integer pageNum);

    int delete(Long id);

    PmsProductCategory getItem(Long id);

    int updateNavStatus(List<Long> ids, Integer navStatus);

    int updateShowStatus(List<Long> ids, Integer showStatus);

    List<PmsProductCategoryWithChildrenItem> listWithChildren();
}
