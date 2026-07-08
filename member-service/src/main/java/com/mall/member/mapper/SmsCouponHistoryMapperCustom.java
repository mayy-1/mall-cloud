package com.mall.member.mapper;

import com.mall.member.model.SmsCoupon;
import com.mall.member.domain.dto.SmsCouponHistoryDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会员优惠券领取历史自定义Mapper
 * Created by macro on 2018/8/29.
 */
public interface SmsCouponHistoryMapperCustom extends SmsCouponHistoryMapper {
    List<SmsCouponHistoryDetail> getDetailList(@Param("memberId") Long memberId);
    List<SmsCoupon> getCouponList(@Param("memberId") Long memberId, @Param("useStatus")Integer useStatus);
}
