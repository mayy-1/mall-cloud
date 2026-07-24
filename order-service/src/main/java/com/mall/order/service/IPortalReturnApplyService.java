package com.mall.order.service;

import com.mall.order.domain.dto.OmsOrderReturnApplyParam;

/**
 * 前台订单退货申请Service
 * Created by macro on 2018/10/17.
 */
public interface IPortalReturnApplyService {
    /**
     * 创建退货申请
     */
    int create(OmsOrderReturnApplyParam returnApply);
}

