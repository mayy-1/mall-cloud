package com.mall.marketing.service.impl;

import com.github.pagehelper.PageHelper;import com.mall.marketing.mapper.SmsCouponProductCategoryRelationMapper;import com.mall.marketing.domain.dto.SmsCouponParam;
import com.mall.marketing.model.*;
import com.mall.marketing.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 优惠券管理Service实现类
 * Created by macro on 2018/8/28.
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements ICouponService {
    /** 优惠券Mapper */
    private final SmsCouponMapper couponMapper;
    /** 优惠券商品分类关联Mapper */
    private final SmsCouponProductCategoryRelationMapper productCategoryRelationMapper;
    /** 优惠券商品关联Mapper */
    private final SmsCouponProductRelationMapper productRelationMapper;
    @Override
    public int create(SmsCouponParam couponParam) {
        couponParam.setCount(couponParam.getPublishCount());
        couponParam.setUseCount(0);
        couponParam.setReceiveCount(0);
        //插入优惠券表
        int count = couponMapper.insert(couponParam);
        //插入优惠券和商品关系表
        if(couponParam.getUseType().equals(2)){
            for(SmsCouponProductRelation productRelation:couponParam.getProductRelationList()){
                productRelation.setCouponId(couponParam.getId());
            }
            productRelationMapper.insertList(couponParam.getProductRelationList());
        }
        //插入优惠券和商品分类关系表
        if(couponParam.getUseType().equals(1)){
            for (SmsCouponProductCategoryRelation couponProductCategoryRelation : couponParam.getProductCategoryRelationList()) {
                couponProductCategoryRelation.setCouponId(couponParam.getId());
            }
            productCategoryRelationMapper.insertList(couponParam.getProductCategoryRelationList());
        }
        return count;
    }

    @Override
    public int delete(Long id) {
        //删除优惠券
        int count = couponMapper.deleteByPrimaryKey(id);
        //删除商品关联
        deleteProductRelation(id);
        //删除商品分类关联
        deleteProductCategoryRelation(id);
        return count;
    }

    private void deleteProductCategoryRelation(Long id) {
        SmsCouponProductCategoryRelation condition = new SmsCouponProductCategoryRelation();
        condition.setCouponId(id);
        productCategoryRelationMapper.deleteByCondition(condition);
    }

    private void deleteProductRelation(Long id) {
        SmsCouponProductRelation condition = new SmsCouponProductRelation();
        condition.setCouponId(id);
        productRelationMapper.deleteByCondition(condition);
    }

    @Override
    public int update(Long id, SmsCouponParam couponParam) {
        couponParam.setId(id);
        int count =couponMapper.updateByPrimaryKey(couponParam);
        //删除后插入优惠券和商品关系表
        if(couponParam.getUseType().equals(2)){
            for(SmsCouponProductRelation productRelation:couponParam.getProductRelationList()){
                productRelation.setCouponId(couponParam.getId());
            }
            deleteProductRelation(id);
            productRelationMapper.insertList(couponParam.getProductRelationList());
        }
        //删除后插入优惠券和商品分类关系表
        if(couponParam.getUseType().equals(1)){
            for (SmsCouponProductCategoryRelation couponProductCategoryRelation : couponParam.getProductCategoryRelationList()) {
                couponProductCategoryRelation.setCouponId(couponParam.getId());
            }
            deleteProductCategoryRelation(id);
            productCategoryRelationMapper.insertList(couponParam.getProductCategoryRelationList());
        }
        return count;
    }

    @Override
    public List<SmsCoupon> list(String name, Integer type, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        SmsCoupon condition = new SmsCoupon();
        if(!StringUtils.isEmpty(name)){
            condition.setName("%"+name+"%");
        }
        if(type!=null){
            condition.setType(type);
        }
        return couponMapper.selectByCondition(condition);
    }

    @Override
    public SmsCouponParam getItem(Long id) {
        return couponMapper.getItem(id);
    }
}
