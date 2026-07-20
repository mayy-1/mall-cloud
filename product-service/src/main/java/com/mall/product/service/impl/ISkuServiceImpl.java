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
        PmsSkuStock skuStock = skuStockMapper.selectByPrimaryKey(skuId);
        if (skuStock == null || skuStock.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }
        skuStock.setStock(skuStock.getStock() - quantity);
        return skuStockMapper.updateByPrimaryKeySelective(skuStock);
    }

    @Override
    public int lockStock(Long skuId, Integer quantity) {
        PmsSkuStock skuStock = skuStockMapper.selectByPrimaryKey(skuId);
        if (skuStock == null) {
            throw new RuntimeException("SKU库存不存在");
        }
        skuStock.setLockStock((skuStock.getLockStock() == null ? 0 : skuStock.getLockStock()) + quantity);
        return skuStockMapper.updateByPrimaryKeySelective(skuStock);
    }

    @Override
    public int releaseStock(Long skuId, Integer quantity) {
        PmsSkuStock skuStock = skuStockMapper.selectByPrimaryKey(skuId);
        if (skuStock == null) {
            throw new RuntimeException("SKU库存不存在");
        }
        skuStock.setLockStock(Math.max((skuStock.getLockStock() == null ? 0 : skuStock.getLockStock()) - quantity, 0));
        return skuStockMapper.updateByPrimaryKeySelective(skuStock);
    }

    @Override
    public List<PmsSkuStock> getSkuStockByProductId(Long productId) {
        PmsSkuStock condition = new PmsSkuStock();
        condition.setProductId(productId);
        return skuStockMapper.selectByCondition(condition);
    }

    @Override
    public int paySuccessDeductStock(Long skuId, Integer quantity) {
        PmsSkuStock skuStock = skuStockMapper.selectByPrimaryKey(skuId);
        if (skuStock == null) {
            throw new RuntimeException("SKU库存不存在");
        }
        skuStock.setStock(skuStock.getStock() - quantity);
        skuStock.setLockStock(Math.max((skuStock.getLockStock() == null ? 0 : skuStock.getLockStock()) - quantity, 0));
        skuStock.setSale((skuStock.getSale() == null ? 0 : skuStock.getSale()) + quantity);
        return skuStockMapper.updateByPrimaryKeySelective(skuStock);
    }
}
