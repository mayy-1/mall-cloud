package com.mall.product.controller;

import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.product.domain.dto.PmsProductCategoryWithChildrenItem;
import com.mall.product.domain.dto.PmsProductParam;
import com.mall.product.domain.dto.PmsProductQueryParam;
import com.mall.product.domain.dto.PmsProductResult;
import com.mall.product.model.PmsBrand;
import com.mall.product.model.PmsProduct;
import com.mall.product.model.PmsProductCategory;
import com.mall.product.service.IBrandService;
import com.mall.product.service.ICategoryService;
import com.mall.product.service.IProductService;
import com.mall.product.service.ISkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理控制器
 * 提供商品CRUD、上下架、推荐、库存扣减、分类/品牌查询相关接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
@Tag(name = "ProductController", description = "商品管理模块")
public class ProductController {

    // 商品业务服务
    private final IProductService productService;
    // SKU库存服务
    private final ISkuService skuStockService;
    // 商品分类服务
    private final ICategoryService productCategoryService;
    // 品牌服务
    private final IBrandService brandService;

    /**
     * 根据商品ID查询商品基础信息
     * @param id 商品主键ID
     * @return 商品基础实体
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id获取商品基础信息")
    public CommonResult<PmsProduct> getItem(@PathVariable Long id) {
        PmsProduct product = productService.getItem(id);
        return CommonResult.success(product);
    }

    /**
     * 获取商品编辑详情（包含SKU、属性、相册等完整数据）
     * @param id 商品主键ID
     * @return 商品完整编辑DTO
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取商品完整编辑详情")
    public CommonResult<PmsProductResult> getDetail(@PathVariable Long id) {
        PmsProductResult productResult = productService.getUpdateInfo(id);
        return CommonResult.success(productResult);
    }

    /**
     * 根据ID批量查询商品列表
     * @param ids 商品ID集合
     * @return 商品基础列表
     */
    @GetMapping("/batch")
    @Operation(summary = "批量获取商品信息")
    public CommonResult<List<PmsProduct>> getListByIds(@RequestParam("ids") List<Long> ids) {
        List<PmsProduct> productList = productService.listByIds(ids);
        return CommonResult.success(productList);
    }

    /**
     * 前台综合商品搜索（仅查询已上架商品）
     * @param keyword 搜索关键词
     * @param brandId 品牌ID（可选）
     * @param productCategoryId 商品分类ID（可选）
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @param sort 排序字段标识（可选）
     * @return 符合条件商品列表
     */
    @GetMapping("/search")
    @Operation(summary = "前台综合搜索商品（仅上架商品）")
    public CommonResult<List<PmsProduct>> search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "brandId", required = false) Long brandId,
            @RequestParam(value = "productCategoryId", required = false) Long productCategoryId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sort", required = false) Integer sort
    ) {
        PmsProductQueryParam queryParam = new PmsProductQueryParam();
        queryParam.setKeyword(keyword);
        queryParam.setBrandId(brandId);
        queryParam.setProductCategoryId(productCategoryId);
        // 仅查询已发布上架商品
        queryParam.setPublishStatus(1);
        List<PmsProduct> productList = productService.list(queryParam, pageSize, pageNum);
        return CommonResult.success(productList);
    }

    /**
     * 查询热门上架商品
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 热门商品列表
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门商品列表")
    public CommonResult<List<PmsProduct>> getHotProducts(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize
    ) {
        PmsProductQueryParam queryParam = new PmsProductQueryParam();
        queryParam.setPublishStatus(1);
        List<PmsProduct> productList = productService.list(queryParam, pageSize, pageNum);
        return CommonResult.success(productList);
    }

    /**
     * 查询新品商品
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 新品商品列表
     */
    @GetMapping("/new")
    @Operation(summary = "获取新品商品列表")
    public CommonResult<List<PmsProduct>> getNewProducts(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize
    ) {
        PmsProductQueryParam queryParam = new PmsProductQueryParam();
        queryParam.setPublishStatus(1);
        queryParam.setNewStatus(1);
        List<PmsProduct> productList = productService.list(queryParam, pageSize, pageNum);
        return CommonResult.success(productList);
    }

    /**
     * 查询推荐商品
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 推荐商品列表
     */
    @GetMapping("/recommend")
    @Operation(summary = "获取推荐商品列表")
    public CommonResult<List<PmsProduct>> getRecommendProducts(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize
    ) {
        PmsProductQueryParam queryParam = new PmsProductQueryParam();
        queryParam.setPublishStatus(1);
        queryParam.setRecommandStatus(1);
        List<PmsProduct> productList = productService.list(queryParam, pageSize, pageNum);
        return CommonResult.success(productList);
    }

    /**
     * 根据品牌ID查询对应上架商品
     * @param brandId 品牌ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 指定品牌商品列表
     */
    @GetMapping("/brand/{brandId}")
    @Operation(summary = "根据品牌id获取对应商品列表")
    public CommonResult<List<PmsProduct>> getBrandProducts(
            @PathVariable Long brandId,
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize
    ) {
        PmsProductQueryParam queryParam = new PmsProductQueryParam();
        queryParam.setBrandId(brandId);
        queryParam.setPublishStatus(1);
        List<PmsProduct> productList = productService.list(queryParam, pageSize, pageNum);
        return CommonResult.success(productList);
    }

    /**
     * SKU库存扣减（下单扣库存）
     * @param skuId SKU主键ID
     * @param quantity 扣减数量
     * @return 操作成功无返回数据
     */
    @PostMapping("/sku/{skuId}/stock/deduct")
    @Operation(summary = "扣减SKU库存")
    public CommonResult<Void> deductStock(
            @PathVariable Long skuId,
            @RequestParam Integer quantity
    ) {
        skuStockService.deductStock(skuId, quantity);
        return CommonResult.success(null);
    }

    /**
     * 获取完整商品分类树形结构（含子分类）
     * @return 树形分类列表
     */
    @GetMapping("/categoryTreeList")
    @Operation(summary = "获取商品分类树形结构")
    public CommonResult<List<PmsProductCategoryWithChildrenItem>> categoryTreeList() {
        List<PmsProductCategoryWithChildrenItem> list = productCategoryService.listWithChildren();
        return CommonResult.success(list);
    }

    /**
     * 根据父分类ID查询子分类列表
     * @param parentId 父分类ID
     * @return 一级子分类列表
     */
    @GetMapping("/category/list/{parentId}")
    @Operation(summary = "分页获取商品子分类列表")
    public CommonResult<List<PmsProductCategory>> getCategoryList(@PathVariable Long parentId) {
        List<PmsProductCategory> categoryList = productCategoryService.getList(parentId, 100, 1);
        return CommonResult.success(categoryList);
    }

    /**
     * 查询推荐品牌列表
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 推荐品牌列表
     */
    @GetMapping("/brand/recommend")
    @Operation(summary = "获取推荐品牌分页列表")
    public CommonResult<List<PmsBrand>> getRecommendBrands(
            @RequestParam("offset") Integer offset,
            @RequestParam("limit") Integer limit
    ) {
        int pageNum = offset / limit + 1;
        List<PmsBrand> brandList = brandService.listRecommendBrand(pageNum, limit);
        return CommonResult.success(brandList);
    }

    /**
     * 新增商品
     * @param productParam 商品完整入参（含SKU、属性、相册等）
     * @return 创建成功行数
     */
    @PostMapping("/create")
    @Operation(summary = "新增商品")
    public CommonResult<Integer> create(@RequestBody PmsProductParam productParam) {
        int count = productService.create(productParam);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    /**
     * 根据商品ID查询编辑详情（同/detail/{id}，后台管理专用）
     * @param id 商品ID
     * @return 商品完整编辑DTO
     */
    @GetMapping("/updateInfo/{id}")
    @Operation(summary = "后台：根据商品id获取编辑信息")
    public CommonResult<PmsProductResult> getUpdateInfo(@PathVariable Long id) {
        PmsProductResult productResult = productService.getUpdateInfo(id);
        return CommonResult.success(productResult);
    }

    /**
     * 修改商品完整信息
     * @param id 商品主键ID
     * @param productParam 商品修改入参
     * @return 修改影响行数
     */
    @PostMapping("/update/{id}")
    @Operation(summary = "修改商品完整信息")
    public CommonResult<Integer> update(
            @PathVariable Long id,
            @RequestBody PmsProductParam productParam
    ) {
        int count = productService.update(id, productParam);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    /**
     * 后台商品分页多条件查询
     * @param productQueryParam 查询条件封装
     * @param pageSize 每页条数，默认5
     * @param pageNum 页码，默认1
     * @return 分页封装商品列表
     */
    @GetMapping("/list")
    @Operation(summary = "后台分页查询商品列表（全状态）")
    public CommonResult<CommonPage<PmsProduct>> getList(
            PmsProductQueryParam productQueryParam,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum
    ) {
        List<PmsProduct> productList = productService.list(productQueryParam, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(productList));
    }

    /**
     * 商品模糊简易查询（名称/货号匹配）
     * @param keyword 名称或货号关键词
     * @return 匹配商品基础列表
     */
    @GetMapping("/simpleList")
    @Operation(summary = "根据商品名称/货号模糊简易查询")
    public CommonResult<List<PmsProduct>> getList(String keyword) {
        List<PmsProduct> productList = productService.list(keyword);
        return CommonResult.success(productList);
    }

    /**
     * 批量修改商品审核状态
     * @param ids 商品ID集合
     * @param verifyStatus 审核状态值
     * @param detail 审核备注说明
     * @return 更新行数
     */
    @PostMapping("/update/verifyStatus")
    @Operation(summary = "批量修改商品审核状态")
    public CommonResult<Integer> updateVerifyStatus(
            @RequestParam("ids") List<Long> ids,
            @RequestParam("verifyStatus") Integer verifyStatus,
            @RequestParam("detail") String detail
    ) {
        int count = productService.updateVerifyStatus(ids, verifyStatus, detail);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    /**
     * 批量上下架商品
     * @param ids 商品ID集合
     * @param publishStatus 发布状态 0下架 1上架
     * @return 更新行数
     */
    @PostMapping("/update/publishStatus")
    @Operation(summary = "批量商品上下架操作")
    public CommonResult<Integer> updatePublishStatus(
            @RequestParam("ids") List<Long> ids,
            @RequestParam("publishStatus") Integer publishStatus
    ) {
        int count = productService.updatePublishStatus(ids, publishStatus);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    /**
     * 批量设置/取消商品推荐
     * @param ids 商品ID集合
     * @param recommendStatus 推荐状态 0不推荐 1推荐
     * @return 更新行数
     */
    @PostMapping("/update/recommendStatus")
    @Operation(summary = "批量设置商品推荐状态")
    public CommonResult<Integer> updateRecommendStatus(
            @RequestParam("ids") List<Long> ids,
            @RequestParam("recommendStatus") Integer recommendStatus
    ) {
        int count = productService.updateRecommendStatus(ids, recommendStatus);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    /**
     * 批量设置/取消新品标识
     * @param ids 商品ID集合
     * @param newStatus 新品状态 0非新品 1新品
     * @return 更新行数
     */
    @PostMapping("/update/newStatus")
    @Operation(summary = "批量设置商品新品状态")
    public CommonResult<Integer> updateNewStatus(
            @RequestParam("ids") List<Long> ids,
            @RequestParam("newStatus") Integer newStatus
    ) {
        int count = productService.updateNewStatus(ids, newStatus);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    /**
     * 批量逻辑删除/恢复商品
     * @param ids 商品ID集合
     * @param deleteStatus 删除状态 0正常 1已删除
     * @return 更新行数
     */
    @PostMapping("/update/deleteStatus")
    @Operation(summary = "批量修改商品删除状态（逻辑删除）")
    public CommonResult<Integer> updateDeleteStatus(
            @RequestParam("ids") List<Long> ids,
            @RequestParam("deleteStatus") Integer deleteStatus
    ) {
        int count = productService.updateDeleteStatus(ids, deleteStatus);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }
}