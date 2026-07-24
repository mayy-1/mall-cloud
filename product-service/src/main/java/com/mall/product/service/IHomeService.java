package com.mall.product.service;

import com.mall.api.dto.ProductCategoryDTO;
import com.mall.api.dto.ProductDTO;
import com.mall.product.domain.vo.HomeContentResult;

import java.util.List;

/**
 * 前台首页内容服务
 */
public interface IHomeService {

    HomeContentResult content();

    List<ProductDTO> recommendProductList(Integer pageSize, Integer pageNum);

    List<ProductCategoryDTO> getProductCateList(Long parentId);

    List<ProductDTO> hotProductList(Integer pageNum, Integer pageSize);

    List<ProductDTO> newProductList(Integer pageNum, Integer pageSize);
}
