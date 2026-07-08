package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsCouponHistory;
import com.mall.marketing.model.SmsCouponHistoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 优惠券领取记录Mapper
 * 提供优惠券领取记录的增删改查操作
 */
public interface SmsCouponHistoryMapper {
    long countByExample(SmsCouponHistoryExample example);

    int deleteByExample(SmsCouponHistoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsCouponHistory row);

    int insertSelective(SmsCouponHistory row);

    List<SmsCouponHistory> selectByExample(SmsCouponHistoryExample example);

    SmsCouponHistory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SmsCouponHistory row, @Param("example") SmsCouponHistoryExample example);

    int updateByExample(@Param("row") SmsCouponHistory row, @Param("example") SmsCouponHistoryExample example);

    int updateByPrimaryKeySelective(SmsCouponHistory row);

    int updateByPrimaryKey(SmsCouponHistory row);
}