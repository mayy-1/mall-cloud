package com.mall.member.mapper;

import com.mall.member.model.SmsCouponProductCategoryRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 优惠券与商品分类关联数据访问接口
 */
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
}