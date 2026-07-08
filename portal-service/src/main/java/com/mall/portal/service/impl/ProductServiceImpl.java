package com.mall.portal.service.impl;

import com.mall.api.dto.BrandDTO;
import com.mall.api.dto.ProductDTO;
import com.mall.portal.model.PmsProduct;
import com.mall.portal.model.PmsBrand;
import com.mall.portal.domain.vo.PmsPortalProductDetail;
import com.mall.portal.domain.vo.PmsProductCategoryNode;
import com.mall.portal.service.IProductService;
import com.mall.api.client.ProductClient;
import com.mall.api.client.BrandClient;
import com.mym.mall.common.api.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台商品管理Service实现类
 * Created by macro on 2020/4/6.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    /** 商品服务Feign客户端 */
    private final ProductClient productClient;
    /** 品牌服务Feign客户端 */
    private final BrandClient brandClient;

    @Override
    public List<PmsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize, Integer sort) {
        try {
            CommonResult<List<ProductDTO>> result = productClient.search(keyword, brandId, productCategoryId, pageNum, pageSize, sort);
            if (result != null && result.getData() != null) {
                return result.getData().stream().map(this::toPmsProduct).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("search products failed", e);
        }
        return List.of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PmsProductCategoryNode> categoryTreeList() {
        try {
            CommonResult<List<Object>> result = productClient.categoryTreeList();
            if (result != null && result.getData() != null) {
                List<PmsProductCategoryNode> nodes = new ArrayList<>();
                for (Object obj : result.getData()) {
                    if (obj instanceof PmsProductCategoryNode) {
                        nodes.add((PmsProductCategoryNode) obj);
                    }
                }
                return nodes;
            }
        } catch (Exception e) {
            log.error("categoryTreeList failed", e);
        }
        return List.of();
    }

    @Override
    public PmsPortalProductDetail detail(Long id) {
        PmsPortalProductDetail detail = new PmsPortalProductDetail();
        try {
            CommonResult<ProductDTO> result = productClient.getById(id);
            if (result != null && result.getData() != null) {
                detail.setProduct(toPmsProduct(result.getData()));
            }
        } catch (Exception e) {
            log.error("detail failed for id={}", id, e);
        }
        return detail;
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
