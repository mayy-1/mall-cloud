package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.marketing.mapper.SmsHomeRecommendProductMapper;
import com.mall.marketing.model.SmsHomeRecommendProduct;import com.mall.marketing.service.IHomeRecommendProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页人气推荐管理Service实现类
 * Created by macro on 2018/11/7.
 */
@Service
@RequiredArgsConstructor
public class HomeRecommendProductServiceImpl implements IHomeRecommendProductService {
    /** 首页人气推荐Mapper */
    private final SmsHomeRecommendProductMapper recommendProductMapper;
    @Override
    public int create(List<SmsHomeRecommendProduct> homeRecommendProductList) {
        for (SmsHomeRecommendProduct recommendProduct : homeRecommendProductList) {
            recommendProduct.setRecommendStatus(1);
            recommendProduct.setSort(0);
            recommendProductMapper.insert(recommendProduct);
        }
        return homeRecommendProductList.size();
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        SmsHomeRecommendProduct recommendProduct = new SmsHomeRecommendProduct();
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
            SmsHomeRecommendProduct record = new SmsHomeRecommendProduct();
            record.setId(id);
            record.setRecommendStatus(recommendStatus);
            recommendProductMapper.updateByPrimaryKeySelective(record);
        }
        return ids.size();
    }

    @Override
    public List<SmsHomeRecommendProduct> list(String productName, Integer recommendStatus, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        SmsHomeRecommendProduct condition = new SmsHomeRecommendProduct();
        if(!StringUtils.isEmpty(productName)){
            condition.setProductName("%"+productName+"%");
        }
        if(recommendStatus!=null){
            condition.setRecommendStatus(recommendStatus);
        }
        return recommendProductMapper.selectByCondition(condition);
    }

    @Override
    public List<Long> getActiveProductIds() {
        SmsHomeRecommendProduct condition = new SmsHomeRecommendProduct();
        condition.setRecommendStatus(1);
        List<SmsHomeRecommendProduct> list = recommendProductMapper.selectByCondition(condition);
        return list.stream().map(SmsHomeRecommendProduct::getProductId).collect(Collectors.toList());
    }
}
