package com.mall.portal.service;

import com.mall.portal.model.CmsSubject;
import com.mall.portal.model.PmsProduct;
import com.mall.portal.model.PmsProductCategory;
import com.mall.portal.domain.vo.HomeContentResult;

import java.util.List;

/**
 * 首页内容管理Service
 * Created by macro on 2019/1/28.
 */
public interface IHomeService {
    HomeContentResult content();

    List<PmsProduct> recommendProductList(Integer pageSize, Integer pageNum);

    List<PmsProductCategory> getProductCateList(Long parentId);

    List<CmsSubject> getSubjectList(Long cateId, Integer pageSize, Integer pageNum);

    List<PmsProduct> hotProductList(Integer pageNum, Integer pageSize);

    List<PmsProduct> newProductList(Integer pageNum, Integer pageSize);
}
