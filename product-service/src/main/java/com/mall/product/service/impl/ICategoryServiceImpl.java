package com.mall.product.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.product.domain.dto.PmsProductCategoryParam;
import com.mall.product.domain.dto.PmsProductCategoryWithChildrenItem;
import com.mall.product.mapper.PmsProductCategoryMapper;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.model.*;
import com.mall.product.service.ICategoryService;
import org.springframework.util.CollectionUtils;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类Service实现类
 * Created by macro on 2018/4/26.
 */
@Service
@RequiredArgsConstructor
public class ICategoryServiceImpl implements ICategoryService {

    /** 商品Mapper */
    private final PmsProductMapper productMapper;

    /** 分类属性关联Mapper（含批量插入） */
    private final PmsProductCategoryAttributeRelationMapper productCategoryAttributeRelationMapper;

    /** 商品分类Mapper（含listWithChildren） */
    private final PmsProductCategoryMapper productCategoryMapper;

    @Override
    public int create(PmsProductCategoryParam pmsProductCategoryParam) {
        PmsProductCategory productCategory = new PmsProductCategory();
        productCategory.setProductCount(0);
        BeanUtils.copyProperties(pmsProductCategoryParam, productCategory);
        //没有父分类时为一级分类
        setCategoryLevel(productCategory);
        int count = productCategoryMapper.insertSelective(productCategory);
        //创建筛选属性关联
        List<Long> productAttributeIdList = pmsProductCategoryParam.getProductAttributeIdList();
        if (!CollectionUtils.isEmpty(productAttributeIdList)) {
            insertRelationList(productCategory.getId(), productAttributeIdList);
        }
        return count;
    }

    /**
     * 批量插入商品分类与筛选属性关系表
     * @param productCategoryId 商品分类id
     * @param productAttributeIdList 相关商品筛选属性id集合
     */
    private void insertRelationList(Long productCategoryId, List<Long> productAttributeIdList) {
        List<PmsProductCategoryAttributeRelation> relationList = new ArrayList<>();
        for (Long productAttrId : productAttributeIdList) {
            PmsProductCategoryAttributeRelation relation = new PmsProductCategoryAttributeRelation();
            relation.setProductAttributeId(productAttrId);
            relation.setProductCategoryId(productCategoryId);
            relationList.add(relation);
        }
        productCategoryAttributeRelationMapper.insertList(relationList);
    }

    @Override
    public int update(Long id, PmsProductCategoryParam pmsProductCategoryParam) {
        PmsProductCategory productCategory = new PmsProductCategory();
        productCategory.setId(id);
        BeanUtils.copyProperties(pmsProductCategoryParam, productCategory);
        setCategoryLevel(productCategory);
        //更新商品分类时要更新商品中的名称
        PmsProduct product = new PmsProduct();
        product.setProductCategoryName(productCategory.getName());
        PmsProduct condition = new PmsProduct();
        condition.setProductCategoryId(id);
        productMapper.updateSelectiveByCondition(product, condition);
        //同时更新筛选属性的信息
        if (!CollectionUtils.isEmpty(pmsProductCategoryParam.getProductAttributeIdList())) {
            PmsProductCategoryAttributeRelation relationCondition = new PmsProductCategoryAttributeRelation();
            relationCondition.setProductCategoryId(id);
            productCategoryAttributeRelationMapper.deleteByCondition(relationCondition);
            insertRelationList(id, pmsProductCategoryParam.getProductAttributeIdList());
        } else {
            PmsProductCategoryAttributeRelation relationCondition = new PmsProductCategoryAttributeRelation();
            relationCondition.setProductCategoryId(id);
            productCategoryAttributeRelationMapper.deleteByCondition(relationCondition);
        }
        return productCategoryMapper.updateByPrimaryKeySelective(productCategory);
    }

    @Override
    public List<PmsProductCategory> getList(Long parentId, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("sort desc");
        PmsProductCategory condition = new PmsProductCategory();
        condition.setParentId(parentId);
        return productCategoryMapper.selectByCondition(condition);
    }

    @Override
    public int delete(Long id) {
        return productCategoryMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PmsProductCategory getItem(Long id) {
        return productCategoryMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateNavStatus(List<Long> ids, Integer navStatus) {
        int count = 0;
        for (Long id : ids) {
            PmsProductCategory category = new PmsProductCategory();
            category.setId(id);
            category.setNavStatus(navStatus);
            count += productCategoryMapper.updateByPrimaryKeySelective(category);
        }
        return count;
    }

    @Override
    public int updateShowStatus(List<Long> ids, Integer showStatus) {
        int count = 0;
        for (Long id : ids) {
            PmsProductCategory category = new PmsProductCategory();
            category.setId(id);
            category.setShowStatus(showStatus);
            count += productCategoryMapper.updateByPrimaryKeySelective(category);
        }
        return count;
    }

    @Override
    public List<PmsProductCategoryWithChildrenItem> listWithChildren() {
        return productCategoryMapper.listWithChildren();
    }

    /**
     * 根据分类的parentId设置分类的level
     */
    private void setCategoryLevel(PmsProductCategory productCategory) {
        //没有父分类时为一级分类
        if (productCategory.getParentId() == 0) {
            productCategory.setLevel(0);
        } else {
            //有父分类时选择根据父分类level设置
            PmsProductCategory parentCategory = productCategoryMapper.selectByPrimaryKey(productCategory.getParentId());
            if (parentCategory != null) {
                productCategory.setLevel(parentCategory.getLevel() + 1);
            } else {
                productCategory.setLevel(0);
            }
        }
    }
}
