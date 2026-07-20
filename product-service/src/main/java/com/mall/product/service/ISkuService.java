package com.mall.product.service;

import com.mall.product.model.PmsSkuStock;

import java.util.List;

/**
 * sku商品库存管理Service
 * 【管理端+订单系统】
 * - 管理端：SKU列表查询(getList)、批量编辑库存(update)
 * - 订单系统：库存扣减(deductStock)、库存锁定(lockStock)、库存释放(releaseStock)、支付成功扣减(paySuccessDeductStock)
 * Created by macro on 2018/4/27.
 */
public interface ISkuService {
    /**
     * 根据产品id和skuCode模糊搜索
     */
    List<PmsSkuStock> getList(Long pid, String keyword);

    /**
     * 批量更新商品库存信息
     */
    int update(Long pid, List<PmsSkuStock> skuStockList);

    /**
     * 扣减sku库存
     */
    int deductStock(Long skuId, Integer quantity);

    /**
     * 锁定SKU库存（lockStock增加）
     */
    int lockStock(Long skuId, Integer quantity);

    /**
     * 释放SKU锁定库存（lockStock减少）
     */
    int releaseStock(Long skuId, Integer quantity);

    /**
     * 根据商品ID查询SKU库存列表
     */
    List<PmsSkuStock> getSkuStockByProductId(Long productId);

    /**
     * 支付成功扣减库存（减stock + 减lockStock + 增sale）
     */
    int paySuccessDeductStock(Long skuId, Integer quantity);
}
