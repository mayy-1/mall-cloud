package com.mall.trade.mapper;

import com.mall.trade.model.OmsOrderReturnApply;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 订单退货申请数据访问接口
 */
public interface OmsOrderReturnApplyMapper {

    int deleteByPrimaryKey(Long id);

    int insert(OmsOrderReturnApply row);

    int insertSelective(OmsOrderReturnApply row);
    OmsOrderReturnApply selectByPrimaryKey(Long id);

    List<OmsOrderReturnApply> selectByCondition(OmsOrderReturnApply record);

    int deleteByCondition(OmsOrderReturnApply record);

    int updateSelectiveByCondition(@Param("record") OmsOrderReturnApply record, @Param("condition") OmsOrderReturnApply condition);

    int updateByPrimaryKeySelective(OmsOrderReturnApply row);

    int updateByPrimaryKey(OmsOrderReturnApply row);
}