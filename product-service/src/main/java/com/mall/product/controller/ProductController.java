package com.mall.product.controller;

import com.mall.api.client.marketing.SubjectClient;
import com.mall.api.dto.CmsSubjectProductRelationDTO;
import com.mall.api.dto.ProductDTO;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品管理核心 Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
@Tag(name = "ProductController", description = "商品管理模块")
public class ProductController {

    private final IProductService productService;
    private final ICategoryService productCategoryService;

    // ==================== 用户端接口 ====================

    /**
     * 【用户端】根据ID查询商品基础信息（已废弃，详情页用 /detail，此接口暂留兼容）
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id获取商品基础信息")
    public CommonResult<PmsProduct> getItem(@PathVariable Long id) {
        return CommonResult.success(productService.getItem(id));
    }

    /** 【Feign】根据ID查询商品DTO */
    @GetMapping("/feign/{id}")
    @Operation(summary = "根据id获取商品DTO")
    public CommonResult<ProductDTO> getDto(@PathVariable Long id) {
        return CommonResult.success(productService.getDto(id));
    }

    /**
     * 【用户端】获取商品完整详情（前台商品详情页，含SKU/属性/阶梯价/满减）
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取商品完整详情")
    public CommonResult<PmsProductResult> getDetail(@PathVariable Long id) {
        PmsProductResult result = productService.getUpdateInfo(id);
        return CommonResult.success(result);
    }

    /**
     * 【用户端】批量查询商品
     */
    @GetMapping("/batch")
    @Operation(summary = "批量获取商品信息")
    public CommonResult<List<PmsProduct>> getListByIds(@RequestParam("ids") List<Long> ids) {
        return CommonResult.success(productService.listByIds(ids));
    }

    /**
     * 【用户端】前台商品搜索
     */

    @Operation(summary = "综合搜索、筛选、排序")
    @Parameter(name = "sort", description = "排序字段:0->按相关度；1->按新品；2->按销量；3->价格从低到高；4->价格从高到低",
            in= ParameterIn.QUERY,schema = @Schema(type = "integer",defaultValue = "0",allowableValues = {"0","1","2","3","4"}))
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<PmsProduct>> search(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) Long brandId,
                                                       @RequestParam(required = false) Long productCategoryId,
                                                       @RequestParam(required = false, defaultValue = "0") Integer pageNum,
                                                       @RequestParam(required = false, defaultValue = "5") Integer pageSize,
                                                       @RequestParam(required = false, defaultValue = "0") Integer sort) {
        List<PmsProduct> productList = productService.search(keyword, brandId, productCategoryId, pageNum, pageSize, sort);
        return CommonResult.success(CommonPage.restPage(productList));
    }

    /**
     * 【用户端】获取分类树形结构（前台分类导航）
     */
    @GetMapping("/categoryTreeList")
    @Operation(summary = "获取商品分类树形结构")
    public CommonResult<List<PmsProductCategoryWithChildrenItem>> categoryTreeList() {
        return CommonResult.success(productCategoryService.listWithChildren());
    }

    // ==================== 管理端接口 ====================

    /**
     * 【管理端】新增商品
     */
    @PostMapping("/create")
    @Operation(summary = "新增商品")
    public CommonResult<Integer> create(@RequestBody PmsProductParam productParam) {
        int count = productService.create(productParam);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    /**
     * 【管理端】商品编辑回显（含SKU、属性、阶梯价、满减、营销标签）
     */
    @GetMapping("/updateInfo/{id}")
    @Operation(summary = "后台：根据商品id获取编辑信息")
    public CommonResult<PmsProductResult> getUpdateInfo(@PathVariable Long id) {
        PmsProductResult result = productService.getUpdateInfo(id);
        return CommonResult.success(result);
    }

    /**
     * 【管理端】修改商品完整信息
     */
    @PostMapping("/update/{id}")
    @Operation(summary = "修改商品完整信息")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody PmsProductParam productParam) {
        int count = productService.update(id, productParam);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    /**
     * 【管理端】商品分页多条件查询（含所有状态商品）
     */
    @GetMapping("/list")
    @Operation(summary = "后台分页查询商品列表（全状态）")
    public CommonResult<CommonPage<PmsProduct>> getList(
            PmsProductQueryParam productQueryParam,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        return CommonResult.success(CommonPage.restPage(productService.list(productQueryParam, pageSize, pageNum)));
    }

    /**
     * 【管理端】批量商品上/下架
     */
    @PostMapping("/update/publishStatus")
    @Operation(summary = "批量商品上下架操作")
    public CommonResult<Integer> updatePublishStatus(
            @RequestParam("ids") List<Long> ids,
            @RequestParam("publishStatus") Integer publishStatus) {
        int count = productService.updatePublishStatus(ids, publishStatus);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    /**
     * 【管理端】批量设置/取消商品推荐
     */
    @PostMapping("/update/recommendStatus")
    @Operation(summary = "批量设置商品推荐状态")
    public CommonResult<Integer> updateRecommendStatus(
            @RequestParam("ids") List<Long> ids,
            @RequestParam("recommendStatus") Integer recommendStatus) {
        int count = productService.updateRecommendStatus(ids, recommendStatus);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    /**
     * 【管理端】批量设置/取消新品标识
     */
    @PostMapping("/update/newStatus")
    @Operation(summary = "批量设置商品新品状态")
    public CommonResult<Integer> updateNewStatus(
            @RequestParam("ids") List<Long> ids,
            @RequestParam("newStatus") Integer newStatus) {
        int count = productService.updateNewStatus(ids, newStatus);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    /**
     * 【管理端】批量逻辑删除/恢复商品
     */
    @PostMapping("/update/deleteStatus")
    @Operation(summary = "批量修改商品删除状态（逻辑删除）")
    public CommonResult<Integer> updateDeleteStatus(
            @RequestParam("ids") List<Long> ids,
            @RequestParam("deleteStatus") Integer deleteStatus) {
        int count = productService.updateDeleteStatus(ids, deleteStatus);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

}