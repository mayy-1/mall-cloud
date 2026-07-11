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
}
