package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.marketing.mapper.SmsHomeAdvertiseMapper;
import com.mall.marketing.model.SmsHomeAdvertise;import com.mall.marketing.service.IHomeAdvertiseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 首页广告管理Service实现类
 * Created by macro on 2018/11/7.
 */
@Service
@RequiredArgsConstructor
public class HomeAdvertiseServiceImpl implements IHomeAdvertiseService {
    /** 首页广告Mapper */
    private final SmsHomeAdvertiseMapper advertiseMapper;

    @Override
    public int create(SmsHomeAdvertise advertise) {
        advertise.setClickCount(0);
        advertise.setOrderCount(0);
        return advertiseMapper.insert(advertise);
    }

    @Override
    public int delete(List<Long> ids) {
        for (Long id : ids) {
            advertiseMapper.deleteByPrimaryKey(id);
        }
        return ids.size();
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        SmsHomeAdvertise record = new SmsHomeAdvertise();
        record.setId(id);
        record.setStatus(status);
        return advertiseMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public SmsHomeAdvertise getItem(Long id) {
        return advertiseMapper.selectByPrimaryKey(id);
    }

    @Override
    public int update(Long id, SmsHomeAdvertise advertise) {
        advertise.setId(id);
        return advertiseMapper.updateByPrimaryKeySelective(advertise);
    }

    @Override
    public List<SmsHomeAdvertise> list(String name, Integer type, String endTime, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        SmsHomeAdvertise condition = new SmsHomeAdvertise();
        if (!StringUtils.isEmpty(name)) {
            condition.setName("%" + name + "%");
        }
        if (type != null) {
            condition.setType(type);
        }
        return advertiseMapper.selectByCondition(condition);
    }
}
