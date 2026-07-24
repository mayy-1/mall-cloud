package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.api.client.product.ProductClient;
import com.mall.api.dto.ProductDTO;
import com.mall.marketing.domain.dto.SmsFlashPromotionProduct;
import com.mall.marketing.mapper.SmsFlashPromotionProductRelationMapper;
import com.mall.marketing.model.SmsFlashPromotionProductRelation;
import com.mall.marketing.service.IFlashPromotionProductRelationService;
import com.mym.mall.common.api.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 限时购商品关联管理Service实现类
 */
@Service
@RequiredArgsConstructor
public class FlashPromotionProductRelationServiceImpl implements IFlashPromotionProductRelationService {
    private final SmsFlashPromotionProductRelationMapper relationMapper;
    private final ProductClient productClient;
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
        List<SmsFlashPromotionProduct> result = new ArrayList<>();
        for (SmsFlashPromotionProductRelation relation : relations) {
            SmsFlashPromotionProduct item = new SmsFlashPromotionProduct();
            // 复制关系字段
            item.setId(relation.getId());
            item.setProductId(relation.getProductId());
            item.setFlashPromotionId(relation.getFlashPromotionId());
            item.setFlashPromotionSessionId(relation.getFlashPromotionSessionId());
            item.setFlashPromotionPrice(relation.getFlashPromotionPrice());
            item.setFlashPromotionCount(relation.getFlashPromotionCount());
            item.setFlashPromotionLimit(relation.getFlashPromotionLimit());
            item.setSort(relation.getSort());
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
