package com.mall.product.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.product.mapper.PmsProductAttributeCategoryMapper;
import com.mall.product.mapper.PmsProductAttributeMapper;
import com.mall.product.domain.dto.PmsProductAttributeParam;
import com.mall.product.domain.dto.ProductAttrInfo;
import com.mall.product.model.PmsProductAttribute;
import com.mall.product.model.PmsProductAttributeCategory;
import com.mall.product.service.IAttributeService;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品属性Service实现类
 * 【管理端专用】商品属性/参数的增删改查
 * 新增/删除属性时自动更新属性分类下的属性计数(attributeCount)或参数计数(paramCount)
 */
@Service
@RequiredArgsConstructor
public class IAttributeServiceImpl implements IAttributeService {
    /** 商品属性Mapper */
    private final PmsProductAttributeMapper productAttributeMapper;
    /** 属性分类Mapper */
    private final PmsProductAttributeCategoryMapper productAttributeCategoryMapper;

    @Override
    public List<PmsProductAttribute> getList(Long cid, Integer type, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("sort desc");
        PmsProductAttribute condition = new PmsProductAttribute();
        condition.setProductAttributeCategoryId(cid);
        condition.setType(type);
        return productAttributeMapper.selectByCondition(condition);
    }

    @Override
    public int create(PmsProductAttributeParam pmsProductAttributeParam) {
        PmsProductAttribute pmsProductAttribute = new PmsProductAttribute();
        BeanUtils.copyProperties(pmsProductAttributeParam, pmsProductAttribute);
        int count = productAttributeMapper.insertSelective(pmsProductAttribute);
        // 新增后更新属性分类计数
        PmsProductAttributeCategory pmsProductAttributeCategory = productAttributeCategoryMapper
                .selectByPrimaryKey(pmsProductAttribute.getProductAttributeCategoryId());
        if (pmsProductAttribute.getType() == 0) {
            pmsProductAttributeCategory.setAttributeCount(pmsProductAttributeCategory.getAttributeCount() + 1);
        } else if (pmsProductAttribute.getType() == 1) {
            pmsProductAttributeCategory.setParamCount(pmsProductAttributeCategory.getParamCount() + 1);
        }
        productAttributeCategoryMapper.updateByPrimaryKey(pmsProductAttributeCategory);
        return count;
    }

    @Override
    public int update(Long id, PmsProductAttributeParam productAttributeParam) {
        PmsProductAttribute pmsProductAttribute = new PmsProductAttribute();
        pmsProductAttribute.setId(id);
        BeanUtils.copyProperties(productAttributeParam, pmsProductAttribute);
        return productAttributeMapper.updateByPrimaryKeySelective(pmsProductAttribute);
    }

    @Override
    public PmsProductAttribute getItem(Long id) {
        return productAttributeMapper.selectByPrimaryKey(id);
    }

    @Override
    public int delete(List<Long> ids) {
        // 获取属性分类信息
        PmsProductAttribute pmsProductAttribute = productAttributeMapper.selectByPrimaryKey(ids.get(0));
        Integer type = pmsProductAttribute.getType();
        PmsProductAttributeCategory pmsProductAttributeCategory = productAttributeCategoryMapper
                .selectByPrimaryKey(pmsProductAttribute.getProductAttributeCategoryId());
        int count = 0;
        for (Long id : ids) {
            count += productAttributeMapper.deleteByPrimaryKey(id);
        }
        // 删除后更新属性分类计数
        if (type == 0) {
            if (pmsProductAttributeCategory.getAttributeCount() >= count) {
                pmsProductAttributeCategory.setAttributeCount(pmsProductAttributeCategory.getAttributeCount() - count);
            } else {
                pmsProductAttributeCategory.setAttributeCount(0);
            }
        } else if (type == 1) {
            if (pmsProductAttributeCategory.getParamCount() >= count) {
                pmsProductAttributeCategory.setParamCount(pmsProductAttributeCategory.getParamCount() - count);
            } else {
                pmsProductAttributeCategory.setParamCount(0);
            }
        }
        productAttributeCategoryMapper.updateByPrimaryKey(pmsProductAttributeCategory);
        return count;
    }

    @Override
    public List<ProductAttrInfo> getProductAttrInfo(Long productCategoryId) {
        return productAttributeMapper.getProductAttrInfo(productCategoryId);
    }
}
