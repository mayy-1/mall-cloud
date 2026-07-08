package com.mall.marketing.mapper;

import com.mall.marketing.domain.dto.SmsCouponParam;
import org.apache.ibatis.annotations.Param;

/**
 * 自定义优惠券管理Mapper
 * Created by macro on 2018/8/29.
 */
public interface SmsCouponMapperCustom extends SmsCouponMapper {
    /**
     * 获取优惠券详情包括绑定关系
     */
    SmsCouponParam getItem(@Param("id") Long id);
}
