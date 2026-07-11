package com.mall.member.mapper;

import com.mall.member.domain.dto.SmsCouponHistoryDetail;
import com.mall.member.model.SmsCoupon;
import com.mall.member.model.SmsCouponHistory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 优惠券领取历史数据访问接口
 */
@Primary
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

    List<SmsCouponHistoryDetail> getDetailList(@Param("memberId") Long memberId);

    List<SmsCoupon> getCouponList(@Param("memberId") Long memberId, @Param("useStatus") Integer useStatus);
}
