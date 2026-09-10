package com.mall.product.service.impl;

import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.product.model.PmsSkuStock;
import com.mall.product.service.ISkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品sku库存管理Service实现类
 * 【管理端+订单系统】
 * - 管理端：getList(SKU列表查询)、update(批量编辑库存，使用REPLACE INTO)
 * - 订单系统：deductStock(扣减库存)、lockStock(锁定库存)、releaseStock(释放库存)、paySuccessDeductStock(支付成功扣减)
 * Created by macro on 2018/4/27.
 */
@Service
@RequiredArgsConstructor
public class ISkuServiceImpl implements ISkuService {
    /** SKU库存Mapper */
    private final PmsSkuStockMapper skuStockMapper;

    @Override
    public List<PmsSkuStock> getList(Long pid, String keyword) {
        PmsSkuStock condition = new PmsSkuStock();
        condition.setProductId(pid);
        if (!StringUtils.isEmpty(keyword)) {
            condition.setSkuCode("%" + keyword + "%");
        }
        return skuStockMapper.selectByCondition(condition);
    }

    @Override
    public int update(Long pid, List<PmsSkuStock> skuStockList) {
        return skuStockMapper.replaceList(skuStockList);
    }

    @Override
    public int deductStock(Long skuId, Integer quantity) {
        int rows = skuStockMapper.deductStock(skuId, quantity);
        if (rows == 0) {
            throw new RuntimeException("库存不足");
        }
        return rows;
    }

    @Override
    public int lockStock(Long skuId, Integer quantity) {
        int rows = skuStockMapper.lockStock(skuId, quantity);
        if (rows == 0) {
            throw new RuntimeException("库存不足，无法锁定");
        }
        return rows;
    }

    @Override
    public int releaseStock(Long skuId, Integer quantity) {
        int rows = skuStockMapper.releaseStock(skuId, quantity);
        if (rows == 0) {
            throw new RuntimeException("SKU库存不存在");
        }
        return rows;
    }

    @Override
    public List<PmsSkuStock> getSkuStockByProductId(Long productId) {
        PmsSkuStock condition = new PmsSkuStock();
        condition.setProductId(productId);
        return skuStockMapper.selectByCondition(condition);
    }

    @Override
    public PmsSkuStock getSkuStockBySkuId(Long skuId) {
        return skuStockMapper.selectByPrimaryKey(skuId);
    }

    @Override
    public List<PmsSkuStock> getSkuStockBySkuIds(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) return List.of();
        return skuStockMapper.selectBySkuIds(skuIds);
    }

    @Override
    public List<PmsSkuStock> getSkuStockByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return List.of();
        return skuStockMapper.selectByProductIds(productIds);
    }

    @Override
    public int paySuccessDeductStock(Long skuId, Integer quantity) {
        int rows = skuStockMapper.paySuccessDeductStock(skuId, quantity);
        if (rows == 0) {
            throw new RuntimeException("库存不足，无法扣减");
        }
        return rows;
    }
}
