package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.marketing.mapper.SmsHomeNewProductMapper;
import com.mall.marketing.model.SmsHomeNewProduct;import com.mall.marketing.service.IHomeNewProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 首页新品推荐管理Service实现类
 * Created by macro on 2018/11/6.
 */
@Service
@RequiredArgsConstructor
public class HomeNewProductServiceImpl implements IHomeNewProductService {
    /** 首页新品Mapper */
    private final SmsHomeNewProductMapper homeNewProductMapper;
    @Override
    public int create(List<SmsHomeNewProduct> homeNewProductList) {
        for (SmsHomeNewProduct SmsHomeNewProduct : homeNewProductList) {
            SmsHomeNewProduct.setRecommendStatus(1);
            SmsHomeNewProduct.setSort(0);
            homeNewProductMapper.insert(SmsHomeNewProduct);
        }
        return homeNewProductList.size();
    }

    @Override
    public int updateSort(Long id, Integer sort) {
        SmsHomeNewProduct homeNewProduct = new SmsHomeNewProduct();
        homeNewProduct.setId(id);
        homeNewProduct.setSort(sort);
        return homeNewProductMapper.updateByPrimaryKeySelective(homeNewProduct);
    }

    @Override
    public int delete(List<Long> ids) {
        for (Long id : ids) {
            homeNewProductMapper.deleteByPrimaryKey(id);
        }
        return ids.size();
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        for (Long id : ids) {
            SmsHomeNewProduct record = new SmsHomeNewProduct();
            record.setId(id);
            record.setRecommendStatus(recommendStatus);
            homeNewProductMapper.updateByPrimaryKeySelective(record);
        }
        return ids.size();
    }

    @Override
    public List<SmsHomeNewProduct> list(String productName, Integer recommendStatus, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        SmsHomeNewProduct condition = new SmsHomeNewProduct();
        if(!StringUtils.isEmpty(productName)){
            condition.setProductName("%"+productName+"%");
        }
        if(recommendStatus!=null){
            condition.setRecommendStatus(recommendStatus);
        }
        return homeNewProductMapper.selectByCondition(condition);
    }
}
