package com.mall.product.service;

import com.mall.api.dto.ProductDTO;
import com.mall.product.domain.dto.PmsProductParam;
import com.mall.product.domain.dto.PmsProductQueryParam;
import com.mall.product.domain.dto.PmsProductResult;
import com.mall.product.model.PmsProduct;
import com.mym.mall.common.api.CommonResult;

import java.util.List;

/**
 * 商品管理Service
 */
public interface IProductService {

    int create(PmsProductParam productParam);

    PmsProductResult getUpdateInfo(Long id);

    int update(Long id, PmsProductParam productParam);

    List<PmsProduct> list(PmsProductQueryParam productQueryParam, Integer pageSize, Integer pageNum);

    int updateVerifyStatus(List<Long> ids, Integer verifyStatus, String detail);

    int updatePublishStatus(List<Long> ids, Integer publishStatus);

    int updateRecommendStatus(List<Long> ids, Integer recommendStatus);

    int updateNewStatus(List<Long> ids, Integer newStatus);

    int updateDeleteStatus(List<Long> ids, Integer deleteStatus);

    List<PmsProduct> list(String keyword);

    PmsProduct getItem(Long id);

    ProductDTO getDto(Long id);

    List<PmsProduct> listByIds(List<Long> ids);

    // ===== 首页聚合专用方法 =====

    CommonResult<List<ProductDTO>> listRecommendProduct(Integer pageNum, Integer pageSize);

    CommonResult<List<ProductDTO>> listHotProduct(Integer pageNum, Integer pageSize);

    CommonResult<List<ProductDTO>> listNewProduct(Integer pageNum, Integer pageSize);

    List<ProductDTO> productList(PmsProductQueryParam param, Integer pageNum, Integer pageSize);

    List<PmsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize, Integer sort);
}
