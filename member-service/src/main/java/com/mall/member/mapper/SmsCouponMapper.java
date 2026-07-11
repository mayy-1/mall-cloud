package com.mall.member.mapper;

import com.mall.member.model.SmsCoupon;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 优惠券数据访问接口
 */
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
}