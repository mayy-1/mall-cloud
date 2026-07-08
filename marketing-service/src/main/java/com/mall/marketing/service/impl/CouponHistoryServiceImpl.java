package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.marketing.mapper.SmsCouponHistoryMapper;
import com.mall.marketing.model.SmsCouponHistory;
import com.mall.marketing.model.SmsCouponHistoryExample;
import com.mall.marketing.service.ICouponHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 优惠券领取记录管理Service实现类
 * Created by macro on 2018/11/6.
 */
@Service
@RequiredArgsConstructor
public class CouponHistoryServiceImpl implements ICouponHistoryService {
    /** 优惠券领取记录Mapper */
    private final SmsCouponHistoryMapper historyMapper;
    @Override
    public List<SmsCouponHistory> list(Long couponId, Integer useStatus, String orderSn, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        SmsCouponHistoryExample example = new SmsCouponHistoryExample();
        SmsCouponHistoryExample.Criteria criteria = example.createCriteria();
        if(couponId!=null){
            criteria.andCouponIdEqualTo(couponId);
        }
        if(useStatus!=null){
            criteria.andUseStatusEqualTo(useStatus);
        }
        if(!StringUtils.isEmpty(orderSn)){
            criteria.andOrderSnEqualTo(orderSn);
        }
        return historyMapper.selectByExample(example);
    }
}
