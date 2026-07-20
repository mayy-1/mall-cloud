package com.mall.product.service;

import com.mall.product.domain.dto.PmsProductAttributeParam;
import com.mall.product.domain.dto.ProductAttrInfo;
import com.mall.product.model.PmsProductAttribute;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品属性Service
 * 【管理端专用】商品属性/参数的增删改查，用于管理端商品属性管理
 * Created by macro on 2018/4/26.
 */
public interface IAttributeService {
    /**
     * 根据分类分页获取商品属性
     * @param cid 分类id
     * @param type 0->属性；2->参数
     */
    List<PmsProductAttribute> getList(Long cid, Integer type, Integer pageSize, Integer pageNum);

    /**
     * 添加商品属性
     */
    @Transactional
    int create(PmsProductAttributeParam pmsProductAttributeParam);

    /**
     * 修改商品属性
     */
    int update(Long id, PmsProductAttributeParam productAttributeParam);

    /**
     * 获取单个商品属性信息
     */
    PmsProductAttribute getItem(Long id);

    /** 批量删除商品属性 */
    @Transactional
    int delete(List<Long> ids);

    /** 根据商品分类ID获取属性信息 */
    List<ProductAttrInfo> getProductAttrInfo(Long productCategoryId);
}
