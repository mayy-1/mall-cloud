package com.mall.product.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.product.domain.dto.PmsProductAttributeCategoryItem;
import com.mall.product.mapper.PmsProductAttributeCategoryMapperCustom;
import com.mall.product.model.PmsProductAttributeCategory;
import com.mall.product.model.PmsProductAttributeCategoryExample;
import com.mall.product.service.IAttributeCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PmsProductAttributeCategoryService瀹炵幇绫? * Created by macro on 2018/4/26.
 */
@Service
@RequiredArgsConstructor
public class IAttributeCategoryServiceImpl implements IAttributeCategoryService {    
    /** 商品属性分类Mapper */
    private final PmsProductAttributeCategoryMapperCustom productAttributeCategoryMapper;

    @Override
    public int create(String name) {
        PmsProductAttributeCategory productAttributeCategory = new PmsProductAttributeCategory();
        productAttributeCategory.setName(name);
        return productAttributeCategoryMapper.insertSelective(productAttributeCategory);
    }

    @Override
    public int update(Long id, String name) {
        PmsProductAttributeCategory productAttributeCategory = new PmsProductAttributeCategory();
        productAttributeCategory.setName(name);
        productAttributeCategory.setId(id);
        return productAttributeCategoryMapper.updateByPrimaryKeySelective(productAttributeCategory);
    }

    @Override
    public int delete(Long id) {
        return productAttributeCategoryMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PmsProductAttributeCategory getItem(Long id) {
        return productAttributeCategoryMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<PmsProductAttributeCategory> getList(Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        return productAttributeCategoryMapper.selectByExample(new PmsProductAttributeCategoryExample());
    }

    @Override
    public List<PmsProductAttributeCategoryItem> getListWithAttr() {
        return productAttributeCategoryMapper.getListWithAttr();
    }
}
