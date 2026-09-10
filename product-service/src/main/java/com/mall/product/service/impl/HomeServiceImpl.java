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
import com.mym.mall.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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

    /** Redis缓存服务 */
    private final RedisService redisService;
    /** Redis数据库前缀 */
    @Value("${redis.database}")
    private String REDIS_DATABASE;
    /** 首页聚合缓存过期时间（秒） */
    @Value("${redis.expire.homeContent}")
    private long REDIS_EXPIRE_HOME_CONTENT;
    /** 首页聚合缓存key */
    @Value("${redis.key.homeContent}")
    private String REDIS_KEY_HOME_CONTENT;
    /** 分类列表缓存过期时间（秒），复用分类树过期时间 */
    @Value("${redis.expire.categoryTree}")
    private long REDIS_EXPIRE_CATEGORY_LIST;
    /** 分类列表缓存key前缀 */
    @Value("${redis.key.categoryList}")
    private String REDIS_KEY_CATEGORY_LIST;

    /** 首页聚合查询线程池 */
    @Autowired
    @Qualifier("homeContentExecutor")
    private Executor homeContentExecutor;

    @Override
    public HomeContentResult content() {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_HOME_CONTENT;
        Object cached = redisService.get(key);
        if (cached != null) {
            return (HomeContentResult) cached;
        }

        // 6 路独立数据源并行加载
        CompletableFuture<List<BrandDTO>> brandFuture =
                CompletableFuture.supplyAsync(this::getRecommendBrands, homeContentExecutor);
        CompletableFuture<List<ProductDTO>> newProductFuture =
                CompletableFuture.supplyAsync(this::getHomeNewProducts, homeContentExecutor);
        CompletableFuture<List<ProductDTO>> hotProductFuture =
                CompletableFuture.supplyAsync(this::getHomeRecommendProducts, homeContentExecutor);
        CompletableFuture<List<SubjectDTO>> subjectFuture =
                CompletableFuture.supplyAsync(this::getSubjects, homeContentExecutor);
        CompletableFuture<HomeFlashPromotionDTO> flashFuture =
                CompletableFuture.supplyAsync(this::getHomeFlashPromotion, homeContentExecutor);
        CompletableFuture<List<HomeAdvertiseDTO>> advertiseFuture =
                CompletableFuture.supplyAsync(this::getHomeAdvertises, homeContentExecutor);

        HomeContentResult result = new HomeContentResult();
        result.setBrandList(brandFuture.join());
        result.setNewProductList(newProductFuture.join());
        result.setHotProductList(hotProductFuture.join());
        result.setSubjectList(subjectFuture.join());
        result.setHomeFlashPromotion(flashFuture.join());
        result.setAdvertiseList(advertiseFuture.join());

        redisService.set(key, result, REDIS_EXPIRE_HOME_CONTENT);
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
        String key = REDIS_DATABASE + ":" + REDIS_KEY_CATEGORY_LIST + ":" + parentId;
        Object cached = redisService.get(key);
        if (cached != null) {
            return (List<ProductCategoryDTO>) cached;
        }
        List<PmsProductCategory> list = categoryService.getList(parentId, 100, 1);
        List<ProductCategoryDTO> result = list.stream().map(this::toDto).collect(Collectors.toList());
        redisService.set(key, result, REDIS_EXPIRE_CATEGORY_LIST);
        return result;
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
        try {
            List<PmsBrand> brands = brandService.listRecommendBrand(0, 6);
            return brands.stream().map(b -> {
                BrandDTO dto = new BrandDTO();
                dto.setId(b.getId());
                dto.setName(b.getName());
                dto.setLogo(b.getLogo());
                dto.setShowStatus(b.getShowStatus());
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("getRecommendBrands failed", e);
            return List.of();
        }
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
