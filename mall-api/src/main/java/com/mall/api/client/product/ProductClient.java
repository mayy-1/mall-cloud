package com.mall.api.client.product;

import com.mall.api.dto.BrandDTO;
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

    /** 扣减SKU库存 */
    @PostMapping("/sku/{skuId}/stock/deduct")
    CommonResult<Void> deductStock(@PathVariable Long skuId, @RequestParam Integer quantity);

    /** 支付成功扣减库存（减stock + 减lockStock + 增sale） */
    @PostMapping("/sku/{skuId}/stock/paySuccess")
    CommonResult<Void> paySuccessDeductStock(@PathVariable Long skuId, @RequestParam Integer quantity);

    /** 锁定SKU库存（lockStock增加） */
    @PostMapping("/sku/{skuId}/stock/lock")
    CommonResult<Void> lockStock(@PathVariable Long skuId, @RequestParam Integer quantity);

    /** 释放SKU锁定库存（lockStock减少） */
    @PostMapping("/sku/{skuId}/stock/release")
    CommonResult<Void> releaseStock(@PathVariable Long skuId, @RequestParam Integer quantity);

    /** 根据商品ID查询SKU库存列表 */
    @GetMapping("/sku/{productId}/list")
    CommonResult<List<SkuStockDTO>> getSkuStockByProductId(@PathVariable Long productId);

    /** 综合搜索商品 */
    @GetMapping("/search")
    CommonResult<List<ProductDTO>> search(@RequestParam("keyword") String keyword,
                                          @RequestParam(value = "brandId", required = false) Long brandId,
                                          @RequestParam(value = "productCategoryId", required = false) Long productCategoryId,
                                          @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                          @RequestParam(value = "sort", required = false) Integer sort);

    /** 分页查询热门商品 */
    @GetMapping("/hot")
    CommonResult<List<ProductDTO>> getHotProducts(@RequestParam("pageNum") Integer pageNum,
                                                   @RequestParam("pageSize") Integer pageSize);

    /** 分页查询新品 */
    @GetMapping("/new")
    CommonResult<List<ProductDTO>> getNewProducts(@RequestParam("pageNum") Integer pageNum,
                                                   @RequestParam("pageSize") Integer pageSize);

    /** 分页查询推荐商品 */
    @GetMapping("/recommend")
    CommonResult<List<ProductDTO>> getRecommendProducts(@RequestParam("pageNum") Integer pageNum,
                                                         @RequestParam("pageSize") Integer pageSize);

    /** 根据品牌查询商品 */
    @GetMapping("/brand/{brandId}")
    CommonResult<List<ProductDTO>> getBrandProducts(@PathVariable Long brandId,
                                                     @RequestParam("pageNum") Integer pageNum,
                                                     @RequestParam("pageSize") Integer pageSize);

    /** 获取商品分类树 */
    @GetMapping("/categoryTreeList")
    CommonResult<List<Object>> categoryTreeList();

    /** 根据父级ID查询子分类 */
    @GetMapping("/category/list/{parentId}")
    CommonResult<List<Object>> getCategoryList(@PathVariable Long parentId);

    /** 查询推荐品牌 */
    @GetMapping("/brand/recommend")
    CommonResult<List<BrandDTO>> getRecommendBrands(@RequestParam("offset") Integer offset,
                                                     @RequestParam("limit") Integer limit);
}
