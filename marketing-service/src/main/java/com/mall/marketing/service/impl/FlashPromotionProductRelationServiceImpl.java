package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.marketing.mapper.SmsFlashPromotionProductRelationMapperCustom;
import com.mall.marketing.domain.dto.SmsFlashPromotionProduct;
import com.mall.marketing.model.SmsFlashPromotionProductRelation;
import com.mall.marketing.model.SmsFlashPromotionProductRelationExample;
import com.mall.marketing.service.IFlashPromotionProductRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 限时购商品关联管理Service实现类
 * Created by macro on 2018/11/16.
 */
@Service
@RequiredArgsConstructor
public class FlashPromotionProductRelationServiceImpl implements IFlashPromotionProductRelationService {
    /** 限时购商品关联自定义Mapper */
    private final SmsFlashPromotionProductRelationMapperCustom relationMapper;
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
        PageHelper.startPage(pageNum,pageSize);
        return relationMapper.getList(flashPromotionId,flashPromotionSessionId);
    }

    @Override
    public long getCount(Long flashPromotionId, Long flashPromotionSessionId) {
        SmsFlashPromotionProductRelationExample example = new SmsFlashPromotionProductRelationExample();
        example.createCriteria()
                .andFlashPromotionIdEqualTo(flashPromotionId)
                .andFlashPromotionSessionIdEqualTo(flashPromotionSessionId);
        return relationMapper.countByExample(example);
    }
}
