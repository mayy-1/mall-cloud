package com.mall.member.mapper;

import com.mall.member.model.SmsCouponHistory;
import com.mall.member.model.SmsCouponHistoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 优惠券领取历史数据访问接口
 */
@Primary
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
