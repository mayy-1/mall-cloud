package com.mall.product.controller;

import com.mym.mall.common.api.CommonResult;
import com.mall.product.model.PmsSkuStock;
import com.mall.product.service.ISkuService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SKU 库存管理 Controller
 *
 * <p>调用方：</p>
 * <ul>
 *   <li>管理端：SKU 列表查询、库存批量编辑</li>
 *   <li>trade-service（Feign）：下单锁库存/扣库存/释放库存/支付扣减</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "SkuController", description = "sku商品库存管理")
@RequestMapping("/sku")
public class SkuController {
    private final ISkuService skuStockService;

    /** 【管理端】根据商品编号查询 SKU 库存列表（支持关键字模糊搜索） */
    @Operation(summary = "根据商品编号及编号模糊搜索sku库存")
    @GetMapping("/{pid}")
    public CommonResult<List<PmsSkuStock>> getList(@PathVariable Long pid, @RequestParam(value = "keyword", required = false) String keyword) {
        return CommonResult.success(skuStockService.getList(pid, keyword));
    }

    /** 【管理端】批量更新 SKU 库存信息 */
    @Operation(summary = "批量更新库存信息")
    @PostMapping("/update/{pid}")
    public CommonResult update(@PathVariable Long pid, @RequestBody List<PmsSkuStock> skuStockList) {
        int count = skuStockService.update(pid, skuStockList);
        return count > 0 ? CommonResult.success(count) : CommonResult.failed();
    }

    // ==================== 库存操作（trade-service Feign 调用） ====================

    /** 【Feign·trade-service】下单扣库存 */
    @PostMapping("/{skuId}/stock/deduct")
    @Operation(summary = "扣减sku库存")
    public CommonResult<Void> deductStock(@PathVariable Long skuId, @RequestParam Integer quantity) {
        skuStockService.deductStock(skuId, quantity);
        return CommonResult.success(null);
    }

    /** 【Feign·trade-service】下单锁定库存 */
    @PostMapping("/{skuId}/stock/lock")
    @Operation(summary = "锁定SKU库存")
    public CommonResult<Void> lockStock(@PathVariable Long skuId, @RequestParam Integer quantity) {
        skuStockService.lockStock(skuId, quantity);
        return CommonResult.success(null);
    }

    /** 【Feign·trade-service】取消订单释放锁定库存 */
    @PostMapping("/{skuId}/stock/release")
    @Operation(summary = "释放SKU锁定库存")
    public CommonResult<Void> releaseStock(@PathVariable Long skuId, @RequestParam Integer quantity) {
        skuStockService.releaseStock(skuId, quantity);
        return CommonResult.success(null);
    }

    /** 【Feign·trade-service】支付成功减库存（stock-1 + lockStock-1 + sale+1） */
    @PostMapping("/{skuId}/stock/paySuccess")
    @Operation(summary = "支付成功扣减库存")
    public CommonResult<Void> paySuccessDeductStock(@PathVariable Long skuId, @RequestParam Integer quantity) {
        skuStockService.paySuccessDeductStock(skuId, quantity);
        return CommonResult.success(null);
    }

    /** 【Feign·trade-service】根据商品ID查询SKU列表 */
    @GetMapping("/{productId}/list")
    @Operation(summary = "根据商品ID查询SKU库存列表")
    public CommonResult<List<PmsSkuStock>> getSkuStockByProductId(@PathVariable Long productId) {
        return CommonResult.success(skuStockService.getSkuStockByProductId(productId));
    }
}

