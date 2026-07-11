package com.mall.order.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.order.mapper.OmsOrderReturnReasonMapper;
import com.mall.order.model.OmsOrderReturnReason;import com.mall.order.service.IOrderReturnReasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 订单原因管理Service实现类
 * Created by macro on 2018/10/17.
 */
@Service
@RequiredArgsConstructor
public class OrderReturnReasonServiceImpl implements IOrderReturnReasonService {
    /** 退货原因Mapper */
    private final OmsOrderReturnReasonMapper returnReasonMapper;
    @Override
    public int create(OmsOrderReturnReason returnReason) {
        returnReason.setCreateTime(new Date());
        return returnReasonMapper.insert(returnReason);
    }

    @Override
    public int update(Long id, OmsOrderReturnReason returnReason) {
        returnReason.setId(id);
        return returnReasonMapper.updateByPrimaryKey(returnReason);
    }

    @Override
    public int delete(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            OmsOrderReturnReason condition = new OmsOrderReturnReason();
            condition.setId(id);
            count += returnReasonMapper.deleteByCondition(condition);
        }
        return count;
    }

    @Override
    public List<OmsOrderReturnReason> list(Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        return returnReasonMapper.selectByCondition(null);
    }

    @Override
    public int updateStatus(List<Long> ids, Integer status) {
        if(!status.equals(0)&&!status.equals(1)){
            return 0;
        }
        int count = 0;
        for (Long id : ids) {
            OmsOrderReturnReason record = new OmsOrderReturnReason();
            record.setId(id);
            record.setStatus(status);
            count += returnReasonMapper.updateByPrimaryKeySelective(record);
        }
        return count;
    }

    @Override
    public OmsOrderReturnReason getItem(Long id) {
        return returnReasonMapper.selectByPrimaryKey(id);
    }
}
