package com.mall.order.mapper;

import com.mall.order.model.OmsOrderReturnReason;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 退货原因Mapper
 * 提供退货原因的增删改查操作
 */
public interface OmsOrderReturnReasonMapper {

    int deleteByPrimaryKey(Long id);

    int insert(OmsOrderReturnReason row);

    int insertSelective(OmsOrderReturnReason row);
    OmsOrderReturnReason selectByPrimaryKey(Long id);

    List<OmsOrderReturnReason> selectByCondition(OmsOrderReturnReason record);

    int deleteByCondition(OmsOrderReturnReason record);

    int updateSelectiveByCondition(@Param("record") OmsOrderReturnReason record, @Param("condition") OmsOrderReturnReason condition);

    int updateByPrimaryKeySelective(OmsOrderReturnReason row);

    int updateByPrimaryKey(OmsOrderReturnReason row);
}