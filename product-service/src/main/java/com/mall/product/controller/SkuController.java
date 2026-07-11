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
 * sku搴撳瓨Controller
 * Created by macro on 2018/4/27.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "SkuController", description = "sku鍟嗗搧搴撳瓨绠＄悊")
@RequestMapping("/sku")
public class SkuController {
    /** SKU库存服务 */
    private final ISkuService skuStockService;

    @Operation(summary = "鏍规嵁鍟嗗搧缂栧彿鍙婄紪鍙锋ā绯婃悳绱ku搴撳瓨")
    @GetMapping("/{pid}")
    public CommonResult<List<PmsSkuStock>> getList(@PathVariable Long pid, @RequestParam(value = "keyword",required = false) String keyword) {
        List<PmsSkuStock> skuStockList = skuStockService.getList(pid, keyword);
        return CommonResult.success(skuStockList);
    }
    @Operation(summary = "鎵归噺鏇存柊搴撳瓨淇℃伅")
    @PostMapping("/update/{pid}")
    public CommonResult update(@PathVariable Long pid,@RequestBody List<PmsSkuStock> skuStockList){
        int count = skuStockService.update(pid,skuStockList);
        if(count>0){
            return CommonResult.success(count);
        }else{
            return CommonResult.failed();
        }
    }

    @Operation(summary = "鎵ｅ噺sku搴撳瓨")
    @PostMapping("/{skuId}/stock/deduct")
    public CommonResult<Void> deductStock(@PathVariable Long skuId, @RequestParam Integer quantity) {
        skuStockService.deductStock(skuId, quantity);
        return CommonResult.success(null);
    }
}
