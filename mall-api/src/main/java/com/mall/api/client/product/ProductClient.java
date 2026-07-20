package com.mall.api.client.product;

import com.mall.api.dto.BrandDTO;
import com.mall.api.dto.ProductCategoryDTO;
import com.mall.api.dto.ProductDTO;
import com.mall.api.dto.SkuStockDTO;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品服务 Feign 接口
 */
@FeignClient(name = "product-service", path = "/product", contextId = "product-product")
public interface ProductClient {

    /** 根据ID查询商品 */
    @GetMapping("/{id}")
    CommonResult<ProductDTO> getById(@PathVariable Long id);

    /** 查询商品详情 */
    @GetMapping("/detail/{id}")
    CommonResult<Object> getDetail(@PathVariable Long id);

    /** 批量查询商品 */
    @GetMapping("/batch")
    CommonResult<List<ProductDTO>> getByIds(@RequestParam("ids") List<Long> ids);

    /** 综合搜索商品 */
    @GetMapping("/search")
    CommonResult<List<ProductDTO>> search(@RequestParam("keyword") String keyword,
                                          @RequestParam(value = "brandId", required = false) Long brandId,
                                          @RequestParam(value = "productCategoryId", required = false) Long productCategoryId,
                                          @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                          @RequestParam(value = "sort", required = false) Integer sort);

    /** 获取商品分类树 */
    @GetMapping("/categoryTreeList")
    CommonResult<List<ProductCategoryDTO>> categoryTreeList();

    /** 根据父级ID查询子分类 */
    @GetMapping("/category/list/{parentId}")
    CommonResult<List<ProductCategoryDTO>> getCategoryList(@PathVariable Long parentId);

    /** 查询推荐品牌 */
    @GetMapping("/brand/recommend")
    CommonResult<List<BrandDTO>> getRecommendBrands(@RequestParam("offset") Integer offset,
                                                     @RequestParam("limit") Integer limit);
}
