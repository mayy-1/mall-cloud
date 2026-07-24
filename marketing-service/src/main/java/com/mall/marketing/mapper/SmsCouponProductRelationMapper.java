package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsCouponProductRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 优惠券商品关联Mapper
 * 提供优惠券与商品关联关系的增删改查操作
 */
@Primary
public interface SmsCouponProductRelationMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SmsCouponProductRelation row);

    int insertSelective(SmsCouponProductRelation row);
    SmsCouponProductRelation selectByPrimaryKey(Long id);

    List<SmsCouponProductRelation> selectByCondition(SmsCouponProductRelation record);

    int deleteByCondition(SmsCouponProductRelation record);

    int updateSelectiveByCondition(@Param("record") SmsCouponProductRelation record, @Param("condition") SmsCouponProductRelation condition);

    int updateByPrimaryKeySelective(SmsCouponProductRelation row);

    int updateByPrimaryKey(SmsCouponProductRelation row);

    int insertList(@Param("list") List<SmsCouponProductRelation> list);

    SmsCouponProductRelation getItem(@Param("id") Long id);
}
