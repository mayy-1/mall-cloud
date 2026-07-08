package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsCoupon;
import com.mall.marketing.model.SmsCouponExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 优惠券Mapper
 * 提供优惠券的增删改查操作
 */
@Primary
public interface SmsCouponMapper {
    long countByExample(SmsCouponExample example);

    int deleteByExample(SmsCouponExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsCoupon row);

    int insertSelective(SmsCoupon row);

    List<SmsCoupon> selectByExample(SmsCouponExample example);

    SmsCoupon selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SmsCoupon row, @Param("example") SmsCouponExample example);

    int updateByExample(@Param("row") SmsCoupon row, @Param("example") SmsCouponExample example);

    int updateByPrimaryKeySelective(SmsCoupon row);

    int updateByPrimaryKey(SmsCoupon row);
}
