package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.marketing.mapper.SmsFlashPromotionMapper;
import com.mall.marketing.model.SmsFlashPromotion;import com.mall.marketing.service.IFlashPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 限时购活动管理Service实现类
 * Created by macro on 2018/11/16.
 */
@Service
@RequiredArgsConstructor
public class FlashPromotionServiceImpl implements IFlashPromotionService {
    /** 限时购活动Mapper */
    private final SmsFlashPromotionMapper flashPromotionMapper;

    @Override
    public int create(SmsFlashPromotion flashPromotion) {
        flashPromotion.setCreateTime(new Date());
        return flashPromotionMapper.insert(flashPromotion);
    }

    @Override
    public int update(Long id, SmsFlashPromotion flashPromotion) {
        flashPromotion.setId(id);
        return flashPromotionMapper.updateByPrimaryKey(flashPromotion);
    }

    @Override
    public int delete(Long id) {
        return flashPromotionMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        SmsFlashPromotion flashPromotion = new SmsFlashPromotion();
        flashPromotion.setId(id);
        flashPromotion.setStatus(status);
        return flashPromotionMapper.updateByPrimaryKeySelective(flashPromotion);
    }

    @Override
    public SmsFlashPromotion getItem(Long id) {
        return flashPromotionMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<SmsFlashPromotion> list(String keyword, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        SmsFlashPromotion condition = new SmsFlashPromotion();
        if (!StringUtils.isEmpty(keyword)) {
            condition.setTitle("%" + keyword + "%");
        }
        return flashPromotionMapper.selectByCondition(condition);
    }

    @Override
    public SmsFlashPromotion getCurrentFlashPromotion() {
        Date now = new Date();
        SmsFlashPromotion condition = new SmsFlashPromotion();
        condition.setStatus(1);
        List<SmsFlashPromotion> flashPromotionList = flashPromotionMapper.selectByCondition(condition);
        if (flashPromotionList != null && !flashPromotionList.isEmpty()) {
            for (SmsFlashPromotion p : flashPromotionList) {
                if (p.getStartDate() != null && !now.before(p.getStartDate())
                        && p.getEndDate() != null && !now.after(p.getEndDate())) {
                    return p;
                }
            }
        }
        return null;
    }
}
