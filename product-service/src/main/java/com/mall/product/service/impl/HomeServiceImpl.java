package com.mall.product.service.impl;

import com.mall.api.client.marketing.HomeClient;
import com.mall.api.client.marketing.MarketingClient;
import com.mall.api.client.marketing.SubjectClient;
import com.mall.api.dto.*;
import com.mall.product.domain.vo.HomeContentResult;
import com.mall.product.model.PmsProductCategory;
import com.mall.product.model.PmsBrand;
import com.mall.product.model.PmsProduct;
import com.mall.product.service.IBrandService;
import com.mall.product.service.ICategoryService;
import com.mall.product.service.IHomeService;
import com.mall.product.service.IProductService;
import com.mym.mall.common.api.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台首页内容服务实现（替代原 portal-service 的 HomeServiceImpl）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements IHomeService {

    private final IProductService productService;
    private final ICategoryService categoryService;
    private final IBrandService brandService;
    private final MarketingClient marketingClient;
    private final SubjectClient subjectClient;
    private final HomeClient homeClient;

    @Override
    public HomeContentResult content() {
        HomeContentResult result = new HomeContentResult();
        try { result.setBrandList(getRecommendBrands()); } catch (Exception e) { log.error("brands failed", e); }
        try { result.setNewProductList(getHomeNewProducts()); } catch (Exception e) { log.error("new products failed", e); }
        try { result.setHotProductList(getHomeRecommendProducts()); } catch (Exception e) { log.error("hot products failed", e); }
        try { result.setSubjectList(getSubjects()); } catch (Exception e) { log.error("subjects failed", e); }
        try { result.setHomeFlashPromotion(getHomeFlashPromotion()); } catch (Exception e) { log.error("flash failed", e); }
        try { result.setAdvertiseList(getHomeAdvertises()); } catch (Exception e) { log.error("ads failed", e); }
        return result;
    }

    @Override
    public List<ProductDTO> recommendProductList(Integer pageSize, Integer pageNum) {
        try {
            CommonResult<List<ProductDTO>> result = productService.listRecommendProduct(pageNum, pageSize);
            return result != null && result.getData() != null ? result.getData() : List.of();
        } catch (Exception e) {
            log.error("recommendProductList failed", e);
        }
        return List.of();
    }

    @Override
    public List<ProductCategoryDTO> getProductCateList(Long parentId) {
        List<PmsProductCategory> list = categoryService.getList(parentId, 100, 1);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> hotProductList(Integer pageNum, Integer pageSize) {
        return getHomeRecommendProducts(pageNum, pageSize);
    }

    @Override
    public List<ProductDTO> newProductList(Integer pageNum, Integer pageSize) {
        return getHomeNewProducts(pageNum, pageSize);
    }

    private List<ProductDTO> getHomeNewProducts() {
        return getHomeNewProducts(1, 4);
    }

    private List<ProductDTO> getHomeNewProducts(Integer pageNum, Integer pageSize) {
        try {
            CommonResult<List<Long>> result = homeClient.getActiveNewProductIds();
            if (result == null || result.getData() == null || result.getData().isEmpty()) return List.of();
            return batchQueryProducts(result.getData(), pageNum, pageSize);
        } catch (Exception e) {
            log.error("getHomeNewProducts failed", e);
            return List.of();
        }
    }

    private List<ProductDTO> getHomeRecommendProducts() {
        return getHomeRecommendProducts(1, 4);
    }

    private List<ProductDTO> getHomeRecommendProducts(Integer pageNum, Integer pageSize) {
        try {
            CommonResult<List<Long>> result = homeClient.getActiveRecommendProductIds();
            if (result == null || result.getData() == null || result.getData().isEmpty()) return List.of();
            return batchQueryProducts(result.getData(), pageNum, pageSize);
        } catch (Exception e) {
            log.error("getHomeRecommendProducts failed", e);
            return List.of();
        }
    }

    private List<ProductDTO> batchQueryProducts(List<Long> ids, Integer pageNum, Integer pageSize) {
        List<PmsProduct> products = productService.listByIds(ids);
        return products.stream()
                .skip((long) (pageNum - 1) * pageSize)
                .limit(pageSize)
                .map(this::toProductDto)
                .collect(Collectors.toList());
    }

    private ProductDTO toProductDto(PmsProduct p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setBrandId(p.getBrandId());
        dto.setBrandName(p.getBrandName());
        dto.setProductCategoryId(p.getProductCategoryId());
        dto.setName(p.getName());
        dto.setSubTitle(p.getSubTitle());
        dto.setPic(p.getPic());
        dto.setPrice(p.getPrice());
        dto.setStock(p.getStock());
        dto.setSale(p.getSale());
        dto.setNewStatus(p.getNewStatus());
        dto.setRecommandStatus(p.getRecommandStatus());
        dto.setProductSn(p.getProductSn());
        return dto;
    }

    private List<BrandDTO> getRecommendBrands() {
        List<PmsBrand> brands = brandService.listRecommendBrand(0, 6);
        return brands.stream().map(b -> {
            BrandDTO dto = new BrandDTO();
            dto.setId(b.getId());
            dto.setName(b.getName());
            dto.setLogo(b.getLogo());
            dto.setShowStatus(b.getShowStatus());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<SubjectDTO> getSubjects() {
        try {
            CommonResult<List<SubjectDTO>> result = subjectClient.listSome();
            return result != null && result.getData() != null ? result.getData() : List.of();
        } catch (Exception e) {
            log.error("getSubjects failed", e);
            return List.of();
        }
    }

    private HomeFlashPromotionDTO getHomeFlashPromotion() {
        try {
            CommonResult<HomeFlashPromotionDTO> result = marketingClient.getHomeFlashPromotion();
            return result != null ? result.getData() : null;
        } catch (Exception e) {
            log.error("getHomeFlashPromotion failed", e);
            return null;
        }
    }

    private List<HomeAdvertiseDTO> getHomeAdvertises() {
        try {
            CommonResult<List<HomeAdvertiseDTO>> result = homeClient.getHomeAdvertises();
            return result != null && result.getData() != null ? result.getData() : List.of();
        } catch (Exception e) {
            log.error("getHomeAdvertises failed", e);
            return List.of();
        }
    }

    private ProductCategoryDTO toDto(PmsProductCategory c) {
        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setId(c.getId());
        dto.setParentId(c.getParentId());
        dto.setName(c.getName());
        dto.setLevel(c.getLevel());
        dto.setProductCount(c.getProductCount());
        dto.setProductUnit(c.getProductUnit());
        dto.setNavStatus(c.getNavStatus());
        dto.setShowStatus(c.getShowStatus());
        dto.setSort(c.getSort());
        dto.setIcon(c.getIcon());
        dto.setKeywords(c.getKeywords());
        dto.setDescription(c.getDescription());
        return dto;
    }
}
