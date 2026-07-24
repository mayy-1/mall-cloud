package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsCouponProductCategoryRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 优惠券商品分类关联Mapper
 * 提供优惠券与商品分类关联关系的增删改查操作
 */
@Primary
public interface SmsCouponProductCategoryRelationMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SmsCouponProductCategoryRelation row);

    int insertSelective(SmsCouponProductCategoryRelation row);
    SmsCouponProductCategoryRelation selectByPrimaryKey(Long id);

    List<SmsCouponProductCategoryRelation> selectByCondition(SmsCouponProductCategoryRelation record);

    int deleteByCondition(SmsCouponProductCategoryRelation record);

    int updateSelectiveByCondition(@Param("record") SmsCouponProductCategoryRelation record, @Param("condition") SmsCouponProductCategoryRelation condition);

    int updateByPrimaryKeySelective(SmsCouponProductCategoryRelation row);

    int updateByPrimaryKey(SmsCouponProductCategoryRelation row);

    int insertList(@Param("list") List<SmsCouponProductCategoryRelation> list);

    SmsCouponProductCategoryRelation getItem(@Param("id") Long id);
}
