package com.mall.product.controller;


import com.mall.api.dto.ProductDTO;
import com.mall.product.domain.dto.PmsProductQueryParam;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.service.IProductService;
import com.mym.mall.common.api.CommonPage;
import com.mym.mall.common.api.CommonResult;
import com.mall.product.domain.dto.PmsBrandParam;
import com.mall.product.model.PmsBrand;
import com.mall.product.service.IBrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 品牌管理 Controller
 * 【管理端+用户端共用】查询类接口同时供用户端前台和管理端使用；
 * 品牌增删改、状态切换为管理端专用
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "BrandController", description = "商品品牌管理")
@RequestMapping("/brand")
public class BrandController {
    private final IBrandService brandService;
    private final IProductService productService;
    private final PmsProductMapper productMapper;

    /** 【管理端+用户端】获取全部品牌列表 */
    @Operation(summary = "获取全部品牌列表")
    @GetMapping("/listAll")
    public CommonResult<List<PmsBrand>> getList() {
        return CommonResult.success(brandService.listAllBrand());
    }

    /** 【管理端】添加品牌 */
    @Operation(summary = "添加品牌")
    @PostMapping("/create")
    public CommonResult create(@Validated @RequestBody PmsBrandParam pmsBrand) {
        CommonResult commonResult;
        int count = brandService.createBrand(pmsBrand);
        if (count == 1) {
            commonResult = CommonResult.success(count);
        } else {
            commonResult = CommonResult.failed();
        }
        return commonResult;
    }

    /** 【管理端】更新品牌 */
    @Operation(summary = "更新品牌")
    @PostMapping("/update/{id}")
    public CommonResult update(@PathVariable("id") Long id,
                               @Validated @RequestBody PmsBrandParam pmsBrandParam) {
        CommonResult commonResult;
        int count = brandService.updateBrand(id, pmsBrandParam);
        if (count == 1) {
            commonResult = CommonResult.success(count);
        } else {
            commonResult = CommonResult.failed();
        }
        return commonResult;
    }

    /** 【管理端】删除品牌 */
    @Operation(summary = "删除品牌")
    @GetMapping("/delete/{id}")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult delete(@PathVariable("id") Long id) {
        int count = brandService.deleteBrand(id);
        if (count == 1) {
            return CommonResult.success(null);
        } else {
            return CommonResult.failed();
        }
    }

    /** 【管理端+用户端】根据品牌名称分页获取品牌列表 */
    @Operation(summary = "根据品牌名称分页获取品牌列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<PmsBrand>> getList(@RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                      @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        List<PmsBrand> brandList = brandService.listBrand(keyword, pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(brandList));
    }

    /** 【管理端+用户端】根据编号查询品牌信息 */
    @Operation(summary = "根据编号查询品牌信息")
    @GetMapping("/{id}")
    public CommonResult<PmsBrand> getItem(@PathVariable("id") Long id) {
        return CommonResult.success(brandService.getBrand(id));
    }

    /** 【管理端】批量删除品牌 */
    @Operation(summary = "批量删除品牌")
    @PostMapping("/delete/batch")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult deleteBatch(@RequestParam("ids") List<Long> ids) {
        int count = brandService.deleteBrand(ids);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /** 【管理端】批量更新显示状态 */
    @Operation(summary = "批量更新显示状态")
    @PostMapping("/update/showStatus")
    public CommonResult updateShowStatus(@RequestParam("ids") List<Long> ids,
                                   @RequestParam("showStatus") Integer showStatus) {
        int count = brandService.updateShowStatus(ids, showStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /** 【管理端】批量更新厂家制造商状态 */
    @Operation(summary = "批量更新厂家制造商状态")
    @PostMapping("/update/factoryStatus")
    public CommonResult updateFactoryStatus(@RequestParam("ids") List<Long> ids,
                                      @RequestParam("factoryStatus") Integer factoryStatus) {
        int count = brandService.updateFactoryStatus(ids, factoryStatus);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    /** 【用户端】分页获取推荐品牌（前台展示用） */
    @Operation(summary = "分页获取推荐品牌")
    @GetMapping("/recommendList")
    public CommonResult<List<PmsBrand>> getRecommendBrands(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                            @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        List<PmsBrand> brandList = brandService.listRecommendBrand(pageNum, pageSize);
        return CommonResult.success(brandList);
    }

    /** 【用户端】根据ID获取品牌详情 */
    @Operation(summary = "获取品牌详情")
    @GetMapping("/detail/{id}")
    public CommonResult<PmsBrand> detail(@PathVariable Long id) {
        return CommonResult.success(brandService.getBrand(id));
    }

    /** 【用户端】分页获取指定品牌下的商品 */
    @Operation(summary = "分页获取品牌商品列表")
    @GetMapping("/productList")
    public CommonResult<List<ProductDTO>> productList(@RequestParam Long brandId,
                                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "6") Integer pageSize) {
        PmsProductQueryParam param = new PmsProductQueryParam();
        param.setBrandId(brandId);
        param.setPublishStatus(1); // 仅查询已上架商品
        List<ProductDTO> result = productService.productList(param,pageNum, pageSize);
        return CommonResult.success(result);
    }

}
