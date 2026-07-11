package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.marketing.mapper.SmsHomeRecommendSubjectMapper;
import com.mall.marketing.model.SmsHomeRecommendSubject;import com.mall.marketing.service.IHomeRecommendSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 首页专题推荐管理Service实现类
 * Created by macro on 2018/11/7.
 */
@Service
@RequiredArgsConstructor
public class HomeRecommendSubjectServiceImpl implements IHomeRecommendSubjectService {
    /** 首页专题推荐Mapper */
    private final SmsHomeRecommendSubjectMapper recommendProductMapper;
    @Override
    public int create(List<SmsHomeRecommendSubject> recommendSubjectList) {
        for (SmsHomeRecommendSubject recommendProduct : recommendSubjectList) {
            recommendProduct.setRecommendStatus(1);
            recommendProduct.setSort(0);
            recommendProductMapper.insert(recommendProduct);
        }
        return recommendSubjectList.size();
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        SmsHomeRecommendSubject recommendProduct = new SmsHomeRecommendSubject();
        recommendProduct.setId(id);
        recommendProduct.setSort(sort);
        return recommendProductMapper.updateByPrimaryKeySelective(recommendProduct);
    }

    @Override
    public int delete(List<Long> ids) {
        for (Long id : ids) {
            recommendProductMapper.deleteByPrimaryKey(id);
        }
        return ids.size();
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        for (Long id : ids) {
            SmsHomeRecommendSubject record = new SmsHomeRecommendSubject();
            record.setId(id);
            record.setRecommendStatus(recommendStatus);
            recommendProductMapper.updateByPrimaryKeySelective(record);
        }
        return ids.size();
    }

    @Override
    public List<SmsHomeRecommendSubject> list(String subjectName, Integer recommendStatus, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        SmsHomeRecommendSubject condition = new SmsHomeRecommendSubject();
        if(!StringUtils.isEmpty(subjectName)){
            condition.setSubjectName("%"+subjectName+"%");
        }
        if(recommendStatus!=null){
            condition.setRecommendStatus(recommendStatus);
        }
        return recommendProductMapper.selectByCondition(condition);
    }
}
