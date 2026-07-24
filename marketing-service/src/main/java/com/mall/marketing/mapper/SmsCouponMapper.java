package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsCoupon;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 优惠券Mapper
 * 提供优惠券的增删改查操作
 */
@Primary
public interface SmsCouponMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SmsCoupon row);

    int insertSelective(SmsCoupon row);
    SmsCoupon selectByPrimaryKey(Long id);

    List<SmsCoupon> selectByCondition(SmsCoupon record);

    int deleteByCondition(SmsCoupon record);

    int updateSelectiveByCondition(@Param("record") SmsCoupon record, @Param("condition") SmsCoupon condition);

    int updateByPrimaryKeySelective(SmsCoupon row);

    int updateByPrimaryKey(SmsCoupon row);

    List<SmsCoupon> getCouponList();
}
