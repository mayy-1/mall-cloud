package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsCouponHistory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 优惠券领取记录Mapper
 * 提供优惠券领取记录的增删改查操作
 */
public interface SmsCouponHistoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SmsCouponHistory row);

    int insertSelective(SmsCouponHistory row);
    SmsCouponHistory selectByPrimaryKey(Long id);

    List<SmsCouponHistory> selectByCondition(SmsCouponHistory record);

    int deleteByCondition(SmsCouponHistory record);

    int updateSelectiveByCondition(@Param("record") SmsCouponHistory record, @Param("condition") SmsCouponHistory condition);

    int updateByPrimaryKeySelective(SmsCouponHistory row);

    int updateByPrimaryKey(SmsCouponHistory row);
}