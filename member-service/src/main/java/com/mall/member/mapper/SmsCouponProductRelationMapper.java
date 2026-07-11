package com.mall.member.mapper;

import com.mall.member.model.SmsCouponProductRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 优惠券与商品关联数据访问接口
 */
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
}