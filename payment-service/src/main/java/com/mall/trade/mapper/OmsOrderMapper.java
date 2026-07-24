package com.mall.trade.mapper;

import com.mall.trade.model.OmsOrder;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 订单数据访问接口
 */
public interface OmsOrderMapper {

    int deleteByPrimaryKey(Long id);

    int insert(OmsOrder row);

    int insertSelective(OmsOrder row);
    OmsOrder selectByPrimaryKey(Long id);

    List<OmsOrder> selectByCondition(OmsOrder record);

    int deleteByCondition(OmsOrder record);

    int updateSelectiveByCondition(@Param("record") OmsOrder record, @Param("condition") OmsOrder condition);

    int updateByPrimaryKeySelective(OmsOrder row);

    int updateByPrimaryKey(OmsOrder row);
}