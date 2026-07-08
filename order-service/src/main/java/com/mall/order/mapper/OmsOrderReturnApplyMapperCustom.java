package com.mall.order.mapper;

import com.mall.order.domain.dto.OmsOrderReturnApplyResult;
import com.mall.order.domain.dto.OmsReturnApplyQueryParam;
import com.mall.order.model.OmsOrderReturnApply;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单退货申请自定义Mapper
 * Created by macro on 2018/10/18.
 */
public interface OmsOrderReturnApplyMapperCustom extends OmsOrderReturnApplyMapper {
    /**
     * 查询申请列表
     */
    List<OmsOrderReturnApply> getList(@Param("queryParam") OmsReturnApplyQueryParam queryParam);

    /**
     * 获取申请详情
     */
    OmsOrderReturnApplyResult getDetail(@Param("id")Long id);
}
