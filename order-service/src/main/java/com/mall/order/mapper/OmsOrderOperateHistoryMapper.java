package com.mall.order.mapper;

import com.mall.order.model.OmsOrderOperateHistory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 订单操作记录Mapper
 * 提供订单操作历史的增删改查操作
 */
@Primary
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

    /** 批量插入操作记录 */
    int insertList(@Param("list") List<OmsOrderOperateHistory> list);
}
