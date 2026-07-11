package com.mall.marketing.service.impl;

import com.mall.marketing.domain.dto.SmsFlashPromotionSessionDetail;
import com.mall.marketing.mapper.SmsFlashPromotionSessionMapper;
import com.mall.marketing.model.SmsFlashPromotionSession;import com.mall.marketing.service.IFlashPromotionProductRelationService;
import com.mall.marketing.service.IFlashPromotionSessionService;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 限时购场次管理Service实现类
 * Created by macro on 2018/11/16.
 */
@Service
@RequiredArgsConstructor
public class FlashPromotionSessionServiceImpl implements IFlashPromotionSessionService {
    /** 限时购场次Mapper */
    private final SmsFlashPromotionSessionMapper promotionSessionMapper;
    /** 限时购商品关联服务 */
    private final IFlashPromotionProductRelationService relationService;

    @Override
    public int create(SmsFlashPromotionSession promotionSession) {
        promotionSession.setCreateTime(new Date());
        return promotionSessionMapper.insert(promotionSession);
    }

    @Override
    public int update(Long id, SmsFlashPromotionSession promotionSession) {
        promotionSession.setId(id);
        return promotionSessionMapper.updateByPrimaryKey(promotionSession);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        SmsFlashPromotionSession promotionSession = new SmsFlashPromotionSession();
        promotionSession.setId(id);
        promotionSession.setStatus(status);
        return promotionSessionMapper.updateByPrimaryKeySelective(promotionSession);
    }

    @Override
    public int delete(Long id) {
        return promotionSessionMapper.deleteByPrimaryKey(id);
    }

    @Override
    public SmsFlashPromotionSession getItem(Long id) {
        return promotionSessionMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<SmsFlashPromotionSession> list() {
        SmsFlashPromotionSession example = new SmsFlashPromotionSession();
        return promotionSessionMapper.selectByCondition(example);
    }

    @Override
    public List<SmsFlashPromotionSessionDetail> selectList(Long flashPromotionId) {
        List<SmsFlashPromotionSessionDetail> result = new ArrayList<>();
        SmsFlashPromotionSession example = new SmsFlashPromotionSession();
        example.setStatus(1);
        List<SmsFlashPromotionSession> list = promotionSessionMapper.selectByCondition(example);
        for (SmsFlashPromotionSession promotionSession : list) {
            SmsFlashPromotionSessionDetail detail = new SmsFlashPromotionSessionDetail();
            BeanUtils.copyProperties(promotionSession, detail);
            long count = relationService.getCount(flashPromotionId, promotionSession.getId());
            detail.setProductCount(count);
            result.add(detail);
        }
        return result;
    }
}
