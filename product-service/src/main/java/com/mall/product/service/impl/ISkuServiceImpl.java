package com.mall.product.service.impl;

import com.mall.product.mapper.PmsSkuStockMapperCustom;
import com.mall.product.model.PmsSkuStock;
import com.mall.product.model.PmsSkuStockExample;
import com.mall.product.service.ISkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 鍟嗗搧sku搴撳瓨绠＄悊Service瀹炵幇绫? * Created by macro on 2018/4/27.
 */
@Service
@RequiredArgsConstructor
public class ISkuServiceImpl implements ISkuService {    
    /** SKU库存Mapper */
    private final PmsSkuStockMapperCustom skuStockMapper;

    @Override
    public List<PmsSkuStock> getList(Long pid, String keyword) {
        PmsSkuStockExample example = new PmsSkuStockExample();
        PmsSkuStockExample.Criteria criteria = example.createCriteria().andProductIdEqualTo(pid);
        if (!StringUtils.isEmpty(keyword)) {
            criteria.andSkuCodeLike("%" + keyword + "%");
        }
        return skuStockMapper.selectByExample(example);
    }

    @Override
    public int update(Long pid, List<PmsSkuStock> skuStockList) {
        return skuStockMapper.replaceList(skuStockList);
    }

    @Override
    public int deductStock(Long skuId, Integer quantity) {
        PmsSkuStock skuStock = skuStockMapper.selectByPrimaryKey(skuId);
        if (skuStock == null || skuStock.getStock() < quantity) {
            throw new RuntimeException("搴撳瓨涓嶈冻");
        }
        skuStock.setStock(skuStock.getStock() - quantity);
        return skuStockMapper.updateByPrimaryKeySelective(skuStock);
    }
}
