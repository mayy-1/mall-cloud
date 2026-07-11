package com.mall.portal.service.impl;

import com.mym.mall.common.api.CommonPage;
import com.mall.api.dto.BrandDTO;
import com.mall.api.dto.ProductDTO;
import com.mall.portal.model.PmsBrand;
import com.mall.portal.model.PmsProduct;
import com.mall.portal.service.IBrandService;
import com.mall.api.client.product.BrandClient;
import com.mall.api.client.product.ProductClient;
import com.mym.mall.common.api.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台品牌管理Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements IBrandService {

    /** 品牌服务Feign客户端 */
    private final BrandClient brandClient;
    /** 商品服务Feign客户端 */
    private final ProductClient productClient;

    @Override
    public List<PmsBrand> recommendList(Integer pageNum, Integer pageSize) {
        try {
            int offset = (pageNum - 1) * pageSize;
            CommonResult<List<BrandDTO>> result = productClient.getRecommendBrands(offset, pageSize);
            if (result != null && result.getData() != null) {
                return result.getData().stream().map(this::toPmsBrand).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("recommendList failed", e);
        }
        return List.of();
    }

    @Override
    public PmsBrand detail(Long brandId) {
        try {
            CommonResult<BrandDTO> result = brandClient.getDetail(brandId);
            if (result != null && result.getData() != null) {
                return toPmsBrand(result.getData());
            }
        } catch (Exception e) {
            log.error("detail failed for brandId={}", brandId, e);
        }
        return null;
    }

    @Override
    public CommonPage<PmsProduct> productList(Long brandId, Integer pageNum, Integer pageSize) {
        try {
            CommonResult<List<ProductDTO>> result = productClient.getBrandProducts(brandId, pageNum, pageSize);
            if (result != null && result.getData() != null) {
                List<PmsProduct> products = result.getData().stream()
                        .map(this::toPmsProduct)
                        .collect(Collectors.toList());
                CommonPage<PmsProduct> page = new CommonPage<>();
                page.setList(products);
                page.setPageNum(pageNum);
                page.setPageSize(pageSize);
                return page;
            }
        } catch (Exception e) {
            log.error("productList failed for brandId={}", brandId, e);
        }
        return new CommonPage<>();
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
}
