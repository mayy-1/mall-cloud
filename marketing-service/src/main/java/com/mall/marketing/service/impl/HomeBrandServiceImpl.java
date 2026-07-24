package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.marketing.mapper.SmsHomeBrandMapper;
import com.mall.marketing.model.SmsHomeBrand;import com.mall.marketing.service.IHomeBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 首页品牌管理Service实现类
 */
@Service
@RequiredArgsConstructor
public class HomeBrandServiceImpl implements IHomeBrandService {
    /** 首页品牌Mapper */
    private final SmsHomeBrandMapper homeBrandMapper;
    @Override
    public int create(List<SmsHomeBrand> homeBrandList) {
        for (SmsHomeBrand smsHomeBrand : homeBrandList) {
            smsHomeBrand.setRecommendStatus(1);
            smsHomeBrand.setSort(0);
            homeBrandMapper.insert(smsHomeBrand);
        }
        return homeBrandList.size();
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        SmsHomeBrand homeBrand = new SmsHomeBrand();
        homeBrand.setId(id);
        homeBrand.setSort(sort);
        return homeBrandMapper.updateByPrimaryKeySelective(homeBrand);
    }

    @Override
    public int delete(List<Long> ids) {
        for (Long id : ids) {
            homeBrandMapper.deleteByPrimaryKey(id);
        }
        return ids.size();
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        for (Long id : ids) {
            SmsHomeBrand record = new SmsHomeBrand();
            record.setId(id);
            record.setRecommendStatus(recommendStatus);
            homeBrandMapper.updateByPrimaryKeySelective(record);
        }
        return ids.size();
    }

    @Override
    public List<SmsHomeBrand> list(String brandName, Integer recommendStatus, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        SmsHomeBrand condition = new SmsHomeBrand();
        if(!StringUtils.isEmpty(brandName)){
            condition.setBrandName("%"+brandName+"%");
        }
        if(recommendStatus!=null){
            condition.setRecommendStatus(recommendStatus);
        }
        return homeBrandMapper.selectByCondition(condition);
    }

    @Override
    public List<SmsHomeBrand> search() {
        SmsHomeBrand condition = new SmsHomeBrand();
        return List.of();
    }
}
