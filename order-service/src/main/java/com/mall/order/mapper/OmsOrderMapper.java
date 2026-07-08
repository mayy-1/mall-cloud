package com.mall.order.mapper;

import com.mall.order.model.OmsOrder;
import com.mall.order.model.OmsOrderExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 订单Mapper
 * 提供订单的增删改查操作
 */
@Primary
public interface OmsOrderMapper {
    long countByExample(OmsOrderExample example);

    int deleteByExample(OmsOrderExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OmsOrder row);

    int insertSelective(OmsOrder row);

    List<OmsOrder> selectByExample(OmsOrderExample example);

    OmsOrder selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") OmsOrder row, @Param("example") OmsOrderExample example);

    int updateByExample(@Param("row") OmsOrder row, @Param("example") OmsOrderExample example);

    int updateByPrimaryKeySelective(OmsOrder row);

    int updateByPrimaryKey(OmsOrder row);
}
