package com.mall.order.mapper;

import com.mall.order.model.OmsOrderOperateHistory;
import com.mall.order.model.OmsOrderOperateHistoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 订单操作记录Mapper
 * 提供订单操作历史的增删改查操作
 */
@Primary
public interface OmsOrderOperateHistoryMapper {
    long countByExample(OmsOrderOperateHistoryExample example);

    int deleteByExample(OmsOrderOperateHistoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OmsOrderOperateHistory row);

    int insertSelective(OmsOrderOperateHistory row);

    List<OmsOrderOperateHistory> selectByExample(OmsOrderOperateHistoryExample example);

    OmsOrderOperateHistory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") OmsOrderOperateHistory row, @Param("example") OmsOrderOperateHistoryExample example);

    int updateByExample(@Param("row") OmsOrderOperateHistory row, @Param("example") OmsOrderOperateHistoryExample example);

    int updateByPrimaryKeySelective(OmsOrderOperateHistory row);

    int updateByPrimaryKey(OmsOrderOperateHistory row);
}
