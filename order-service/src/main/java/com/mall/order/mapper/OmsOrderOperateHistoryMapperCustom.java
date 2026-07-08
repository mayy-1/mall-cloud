package com.mall.order.mapper;

import com.mall.order.model.OmsOrderOperateHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单操作记录自定义Mapper
 * Created by macro on 2018/10/12.
 */
public interface OmsOrderOperateHistoryMapperCustom extends OmsOrderOperateHistoryMapper {
    /**
     * 批量创建
     */
    int insertList(@Param("list") List<OmsOrderOperateHistory> orderOperateHistoryList);
}
