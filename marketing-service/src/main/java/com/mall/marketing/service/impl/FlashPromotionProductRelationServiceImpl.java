package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.api.client.product.ProductClient;
import com.mall.api.client.product.SkuStockClient;
import com.mall.api.dto.ProductDTO;
import com.mall.api.dto.SkuStockDTO;
import com.mall.marketing.domain.dto.SmsFlashPromotionProduct;
import com.mall.marketing.mapper.SmsFlashPromotionProductRelationMapper;
import com.mall.marketing.model.SmsFlashPromotionProductRelation;
import com.mall.marketing.service.IFlashPromotionProductRelationService;
import com.mym.mall.common.api.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 限时购商品关联管理Service实现类
 */
@Service
@RequiredArgsConstructor
public class FlashPromotionProductRelationServiceImpl implements IFlashPromotionProductRelationService {
    private final SmsFlashPromotionProductRelationMapper relationMapper;
    private final ProductClient productClient;
    private final SkuStockClient skuStockClient;
    @Override
    public int create(List<SmsFlashPromotionProductRelation> relationList) {
        for (SmsFlashPromotionProductRelation relation : relationList) {
            relationMapper.insert(relation);
        }
        return relationList.size();
    }

    @Override
    public int update(Long id, SmsFlashPromotionProductRelation relation) {
        relation.setId(id);
        return relationMapper.updateByPrimaryKey(relation);
    }

    @Override
    public int delete(Long id) {
        return relationMapper.deleteByPrimaryKey(id);
    }

    @Override
    public SmsFlashPromotionProductRelation getItem(Long id) {
        return relationMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<SmsFlashPromotionProduct> list(Long flashPromotionId, Long flashPromotionSessionId, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        List<SmsFlashPromotionProductRelation> relations = relationMapper.getList(flashPromotionId, flashPromotionSessionId);

        // 批量收集 skuId，查 SKU 详情填 spData（一次远程调用，避免 N 次）
        List<Long> skuIds = relations.stream()
                .map(SmsFlashPromotionProductRelation::getSkuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, SkuStockDTO> skuMap = new HashMap<>();
        if (!skuIds.isEmpty()) {
            try {
                CommonResult<List<SkuStockDTO>> skuRes = skuStockClient.getSkuStockBySkuIds(skuIds);
                if (skuRes != null && skuRes.getData() != null) {
                    for (SkuStockDTO sku : skuRes.getData()) {
                        if (sku.getId() != null) skuMap.put(sku.getId(), sku);
                    }
                }
            } catch (Exception e) {
                // SKU 查不到不影响主流程
            }
        }

        List<SmsFlashPromotionProduct> result = new ArrayList<>();
        for (SmsFlashPromotionProductRelation relation : relations) {
            SmsFlashPromotionProduct item = new SmsFlashPromotionProduct();
            // 复制关系字段
            item.setId(relation.getId());
            item.setProductId(relation.getProductId());
            item.setSkuId(relation.getSkuId());
            item.setFlashPromotionId(relation.getFlashPromotionId());
            item.setFlashPromotionSessionId(relation.getFlashPromotionSessionId());
            item.setFlashPromotionPrice(relation.getFlashPromotionPrice());
            item.setFlashPromotionCount(relation.getFlashPromotionCount());
            item.setFlashPromotionLimit(relation.getFlashPromotionLimit());
            item.setSort(relation.getSort());
            // 填 SKU spData（规格文字）
            if (relation.getSkuId() != null) {
                SkuStockDTO sku = skuMap.get(relation.getSkuId());
                if (sku != null) {
                    item.setSpData(sku.getSpData());
                }
            }
            // Feign 查商品信息
            try {
                CommonResult<ProductDTO> productResult = productClient.getById(relation.getProductId());
                if (productResult != null) {
                    item.setProduct(productResult.getData());
                }
            } catch (Exception e) {
                // 商品查不到，跳过
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public long getCount(Long flashPromotionId, Long flashPromotionSessionId) {
        SmsFlashPromotionProductRelation condition = new SmsFlashPromotionProductRelation();
        condition.setFlashPromotionId(flashPromotionId);
        condition.setFlashPromotionSessionId(flashPromotionSessionId);
        return relationMapper.selectByCondition(condition).size();
    }
}
