package com.mall.product.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.product.domain.dto.PmsBrandParam;
import com.mall.product.mapper.PmsBrandMapper;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.model.PmsBrand;
import com.mall.product.model.PmsProduct;
import com.mall.product.service.IBrandService;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品品牌Service实现类
 * Created by macro on 2018/4/26.
 */
@Service
@RequiredArgsConstructor
public class IBrandServiceImpl implements IBrandService {

    /** 品牌Mapper */
    private final PmsBrandMapper brandMapper;

    private final PmsProductMapper productMapper;

    @Override
    public List<PmsBrand> listAllBrand() {
        return brandMapper.selectByCondition(new PmsBrand());
    }

    @Override
    public int createBrand(PmsBrandParam pmsBrandParam) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtils.copyProperties(pmsBrandParam, pmsBrand);
        //如果创建时首字母为空，取名称的第一个为首字母
        if (StringUtils.isEmpty(pmsBrand.getFirstLetter())) {
            pmsBrand.setFirstLetter(pmsBrand.getName().substring(0, 1));
        }
        return brandMapper.insertSelective(pmsBrand);
    }

    @Override
    public int updateBrand(Long id, PmsBrandParam pmsBrandParam) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtils.copyProperties(pmsBrandParam, pmsBrand);
        pmsBrand.setId(id);
        //如果创建时首字母为空，取名称的第一个为首字母
        if (StringUtils.isEmpty(pmsBrand.getFirstLetter())) {
            pmsBrand.setFirstLetter(pmsBrand.getName().substring(0, 1));
        }
        //更新品牌时要更新商品中的品牌名称
        PmsProduct product = new PmsProduct();
        product.setBrandName(pmsBrand.getName());
        PmsProduct condition = new PmsProduct();
        condition.setBrandId(id);
        productMapper.updateSelectiveByCondition(product, condition);
        return brandMapper.updateByPrimaryKeySelective(pmsBrand);
    }

    @Override
    public int deleteBrand(Long id) {
        return brandMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int deleteBrand(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            count += brandMapper.deleteByPrimaryKey(id);
        }
        return count;
    }

    @Override
    public List<PmsBrand> listBrand(String keyword, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("sort desc");
        PmsBrand condition = new PmsBrand();
        if (!StringUtils.isEmpty(keyword)) {
            condition.setName("%" + keyword + "%");
        }
        return brandMapper.selectByCondition(condition);
    }

    @Override
    public PmsBrand getBrand(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateShowStatus(List<Long> ids, Integer showStatus) {
        int count = 0;
        for (Long id : ids) {
            PmsBrand brand = new PmsBrand();
            brand.setId(id);
            brand.setShowStatus(showStatus);
            count += brandMapper.updateByPrimaryKeySelective(brand);
        }
        return count;
    }

    @Override
    public int updateFactoryStatus(List<Long> ids, Integer factoryStatus) {
        int count = 0;
        for (Long id : ids) {
            PmsBrand brand = new PmsBrand();
            brand.setId(id);
            brand.setFactoryStatus(factoryStatus);
            count += brandMapper.updateByPrimaryKeySelective(brand);
        }
        return count;
    }

    @Override
    public List<PmsBrand> listRecommendBrand(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("sort desc");
        PmsBrand condition = new PmsBrand();
        condition.setShowStatus(1);
        return brandMapper.selectByCondition(condition);
    }
}
