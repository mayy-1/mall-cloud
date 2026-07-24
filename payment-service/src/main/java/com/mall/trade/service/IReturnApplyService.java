package com.mall.trade.service;

import com.mall.trade.domain.dto.OmsOrderReturnApplyParam;

/**
 * 前台订单退货申请Service
 * Created by macro on 2018/10/17.
 */
public interface IReturnApplyService {
    /**
     * 创建退货申请
     */
    int create(OmsOrderReturnApplyParam returnApply);
}
