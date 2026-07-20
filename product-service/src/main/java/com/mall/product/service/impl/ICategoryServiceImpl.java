package com.mall.product.service.impl;

import com.github.pagehelper.PageHelper;
import com.mall.product.domain.dto.PmsProductCategoryFlatItem;
import com.mall.product.domain.dto.PmsProductCategoryParam;
import com.mall.product.domain.dto.PmsProductCategoryWithChildrenItem;
import com.mall.product.mapper.PmsProductCategoryAttributeRelationMapper;
import com.mall.product.mapper.PmsProductCategoryMapper;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.model.*;
import com.mall.product.service.ICategoryService;
import org.springframework.util.CollectionUtils;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品分类Service实现类
 * 【管理端+用户端】listWithChildren为用户端共用（分类层级树），
 * 分类增删改、导航/显示状态切换为管理端专用，更新分类时同步更新商品中的分类名称
 */
@Service
@RequiredArgsConstructor
public class ICategoryServiceImpl implements ICategoryService {

    private final PmsProductMapper productMapper;

    private final PmsProductCategoryAttributeRelationMapper productCategoryAttributeRelationMapper;

    private final PmsProductCategoryMapper productCategoryMapper;

    @Override
    @Transactional
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
    @Transactional
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
        PmsProductCategory record = new PmsProductCategory();
        record.setNavStatus(navStatus);
        return productCategoryMapper.updateByIds(record, ids);
    }

    @Override
    public int updateShowStatus(List<Long> ids, Integer showStatus) {
        PmsProductCategory record = new PmsProductCategory();
        record.setShowStatus(showStatus);
        return productCategoryMapper.updateByIds(record, ids);
    }

    @Override
    public List<PmsProductCategoryWithChildrenItem> listWithChildren() {
        // 1. 从 Mapper 获取扁平数据（一级分类 + 直接子分类，按行展开）
        List<PmsProductCategoryFlatItem> flatList = productCategoryMapper.listWithChildren();
        if (CollectionUtils.isEmpty(flatList)) {
            return new ArrayList<>();
        }
        // 2. 在内存中按一级分类ID分组，构建树形结构
        Map<Long, PmsProductCategoryWithChildrenItem> treeMap = new LinkedHashMap<>();
        for (PmsProductCategoryFlatItem flat : flatList) {
            PmsProductCategoryWithChildrenItem parent = treeMap.get(flat.getId());
            if (parent == null) {
                parent = new PmsProductCategoryWithChildrenItem();
                parent.setId(flat.getId());
                parent.setName(flat.getName());
                parent.setChildren(new ArrayList<>());
                treeMap.put(flat.getId(), parent);
            }
            // LEFT JOIN 的一级分类自身没有子分类时 child_id 为 NULL，过滤掉
            if (flat.getChildId() != null) {
                PmsProductCategory child = new PmsProductCategory();
                child.setId(flat.getChildId());
                child.setName(flat.getChildName());
                parent.getChildren().add(child);
            }
        }
        return new ArrayList<>(treeMap.values());
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
