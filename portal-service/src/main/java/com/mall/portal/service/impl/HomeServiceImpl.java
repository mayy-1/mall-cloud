package com.mall.portal.service.impl;

import com.mall.api.dto.BrandDTO;
import com.mall.api.dto.ProductDTO;
import com.mall.api.dto.SubjectDTO;
import com.mall.portal.model.CmsSubject;
import com.mall.portal.model.PmsBrand;
import com.mall.portal.model.PmsProduct;
import com.mall.portal.model.PmsProductCategory;
import com.mall.portal.model.SmsHomeAdvertise;
import com.mall.portal.domain.vo.HomeContentResult;
import com.mall.portal.domain.vo.HomeFlashPromotion;
import com.mall.portal.service.IHomeService;
import com.mall.api.client.product.ProductClient;
import com.mall.api.client.marketing.SubjectClient;
import com.mall.api.client.marketing.HomeClient;
import com.mall.api.client.marketing.MarketingClient;
import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页内容管理Service实现类
 * Created by macro on 2019/1/28.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements IHomeService {

    /** 商品服务Feign客户端 */
    private final ProductClient productClient;
    /** 专题服务Feign客户端 */
    private final SubjectClient subjectClient;
    /** 首页服务Feign客户端 */
    private final HomeClient homeClient;
    /** 营销服务Feign客户端 */
    private final MarketingClient marketingClient;

    @Override
    public HomeContentResult content() {
        HomeContentResult result = new HomeContentResult();
        try {
            result.setBrandList(getRecommendBrands());
        } catch (Exception e) {
            log.error("Failed to load recommend brands", e);
        }
        try {
            result.setNewProductList(getNewProducts(0, 4));
        } catch (Exception e) {
            log.error("Failed to load new products", e);
        }
        try {
            result.setHotProductList(getHotProducts(0, 4));
        } catch (Exception e) {
            log.error("Failed to load hot products", e);
        }
        try {
            result.setSubjectList(getSubjects());
        } catch (Exception e) {
            log.error("Failed to load subjects", e);
        }
        try {
            result.setHomeFlashPromotion(getHomeFlashPromotion());
        } catch (Exception e) {
            log.error("Failed to load home flash promotion", e);
        }
        try {
            result.setAdvertiseList(getHomeAdvertises());
        } catch (Exception e) {
            log.error("Failed to load home advertises", e);
        }
        return result;
    }

    @Override
    public List<PmsProduct> recommendProductList(Integer pageSize, Integer pageNum) {
        try {
            CommonResult<List<ProductDTO>> result = productClient.getRecommendProducts(pageNum, pageSize);
            if (result != null && result.getData() != null) {
                return result.getData().stream().map(this::toPmsProduct).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("recommendProductList failed", e);
        }
        return List.of();
    }

    @Override
    public List<PmsProductCategory> getProductCateList(Long parentId) {
        try {
            CommonResult<List<Object>> result = productClient.getCategoryList(parentId);
            if (result != null && result.getData() != null) {
                List<PmsProductCategory> categories = new ArrayList<>();
                for (Object obj : result.getData()) {
                    if (obj instanceof PmsProductCategory) {
                        categories.add((PmsProductCategory) obj);
                    }
                }
                return categories;
            }
        } catch (Exception e) {
            log.error("getProductCateList failed for parentId={}", parentId, e);
        }
        return List.of();
    }

    @Override
    public List<CmsSubject> getSubjectList(Long cateId, Integer pageSize, Integer pageNum) {
        try {
            CommonResult<CommonPage<SubjectDTO>> result = subjectClient.list(null, pageNum, pageSize);
            if (result != null && result.getData() != null && result.getData().getList() != null) {
                return result.getData().getList().stream().map(this::toCmsSubject).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("getSubjectList failed", e);
        }
        return List.of();
    }

    @Override
    public List<PmsProduct> hotProductList(Integer pageNum, Integer pageSize) {
        return getHotProducts(pageNum, pageSize);
    }

    @Override
    public List<PmsProduct> newProductList(Integer pageNum, Integer pageSize) {
        return getNewProducts(pageNum, pageSize);
    }

    /** 通过Feign调用商品服务获取人气推荐商品 */
    private List<PmsProduct> getHotProducts(Integer pageNum, Integer pageSize) {
        try {
            CommonResult<List<ProductDTO>> result = productClient.getHotProducts(pageNum, pageSize);
            if (result != null && result.getData() != null) {
                return result.getData().stream().map(this::toPmsProduct).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("getHotProducts failed", e);
        }
        return List.of();
    }

    /** 通过Feign调用商品服务获取新品推荐 */
    private List<PmsProduct> getNewProducts(Integer pageNum, Integer pageSize) {
        try {
            CommonResult<List<ProductDTO>> result = productClient.getNewProducts(pageNum, pageSize);
            if (result != null && result.getData() != null) {
                return result.getData().stream().map(this::toPmsProduct).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("getNewProducts failed", e);
        }
        return List.of();
    }

    /** 通过Feign调用商品服务获取推荐品牌 */
    private List<PmsBrand> getRecommendBrands() {
        try {
            CommonResult<List<BrandDTO>> result = productClient.getRecommendBrands(0, 6);
            if (result != null && result.getData() != null) {
                return result.getData().stream().map(this::toPmsBrand).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("getRecommendBrands failed", e);
        }
        return List.of();
    }

    /** 通过Feign调用专题服务获取专题列表 */
    private List<CmsSubject> getSubjects() {
        try {
            CommonResult<List<SubjectDTO>> result = subjectClient.listAll();
            if (result != null && result.getData() != null) {
                return result.getData().stream().map(this::toCmsSubject).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("getSubjects failed", e);
        }
        return List.of();
    }

    /** 通过Feign调用营销服务获取首页秒杀信息 */
    private HomeFlashPromotion getHomeFlashPromotion() {
        try {
            CommonResult<Object> result = marketingClient.getHomeFlashPromotion();
            if (result != null && result.getData() instanceof HomeFlashPromotion) {
                return (HomeFlashPromotion) result.getData();
            }
        } catch (Exception e) {
            log.error("getHomeFlashPromotion failed", e);
        }
        return null;
    }

    /** 通过Feign调用首页服务获取轮播广告 */
    @SuppressWarnings("unchecked")
    private List<SmsHomeAdvertise> getHomeAdvertises() {
        try {
            CommonResult<List<Object>> result = homeClient.getHomeAdvertises();
            if (result != null && result.getData() != null) {
                List<SmsHomeAdvertise> advertises = new ArrayList<>();
                for (Object obj : result.getData()) {
                    if (obj instanceof SmsHomeAdvertise) {
                        advertises.add((SmsHomeAdvertise) obj);
                    }
                }
                return advertises;
            }
        } catch (Exception e) {
            log.error("getHomeAdvertises failed", e);
        }
        return List.of();
    }

    /** DTO转前端商品实体 */
    private PmsProduct toPmsProduct(ProductDTO dto) {
        if (dto == null) return null;
        PmsProduct product = new PmsProduct();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setPic(dto.getPic());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setBrandName(dto.getBrandName());
        product.setProductSn(dto.getProductSn());
        return product;
    }

    /** DTO转前端品牌实体 */
    private PmsBrand toPmsBrand(BrandDTO dto) {
        if (dto == null) return null;
        PmsBrand brand = new PmsBrand();
        brand.setId(dto.getId());
        brand.setName(dto.getName());
        brand.setLogo(dto.getLogo());
        brand.setShowStatus(dto.getShowStatus());
        return brand;
    }

    /** DTO转前端专题实体 */
    private CmsSubject toCmsSubject(SubjectDTO dto) {
        if (dto == null) return null;
        CmsSubject subject = new CmsSubject();
        subject.setId(dto.getId());
        subject.setTitle(dto.getTitle());
        subject.setPic(dto.getPic());
        return subject;
    }
}
