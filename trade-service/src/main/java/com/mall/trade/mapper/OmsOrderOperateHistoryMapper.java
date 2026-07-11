package com.mall.trade.mapper;

import com.mall.trade.model.OmsOrderOperateHistory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 订单操作历史数据访问接口
 */
public interface OmsOrderOperateHistoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(OmsOrderOperateHistory row);

    int insertSelective(OmsOrderOperateHistory row);
    OmsOrderOperateHistory selectByPrimaryKey(Long id);

    List<OmsOrderOperateHistory> selectByCondition(OmsOrderOperateHistory record);

    int deleteByCondition(OmsOrderOperateHistory record);

    int updateSelectiveByCondition(@Param("record") OmsOrderOperateHistory record, @Param("condition") OmsOrderOperateHistory condition);

    int updateByPrimaryKeySelective(OmsOrderOperateHistory row);

    int updateByPrimaryKey(OmsOrderOperateHistory row);
}