package com.mall.api.client.product;

import com.mall.api.dto.SkuStockDTO;
import com.mym.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SKU 库存 Feign 接口
 * 供 order-service 下单/支付/取消时操作库存
 */
@FeignClient(name = "product-service", path = "/sku", contextId = "product-sku")
public interface SkuStockClient {

    /** 根据商品ID查询SKU列表 */
    @GetMapping("/{productId}/list")
    CommonResult<List<SkuStockDTO>> getSkuStockByProductId(@PathVariable Long productId);

    /** 下单扣库存 */
    @PostMapping("/{skuId}/stock/deduct")
    CommonResult<Void> deductStock(@PathVariable Long skuId, @RequestParam Integer quantity);

    /** 下单锁定库存 */
    @PostMapping("/{skuId}/stock/lock")
    CommonResult<Void> lockStock(@PathVariable Long skuId, @RequestParam Integer quantity);

    /** 取消订单释放锁定库存 */
    @PostMapping("/{skuId}/stock/release")
    CommonResult<Void> releaseStock(@PathVariable Long skuId, @RequestParam Integer quantity);

    /** 支付成功扣减库存 */
    @PostMapping("/{skuId}/stock/paySuccess")
    CommonResult<Void> paySuccessDeductStock(@PathVariable Long skuId, @RequestParam Integer quantity);
}
