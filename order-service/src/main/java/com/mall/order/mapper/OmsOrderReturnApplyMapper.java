package com.mall.order.mapper;

import com.mall.order.model.OmsOrderReturnApply;
import com.mall.order.model.OmsOrderReturnApplyExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 订单退货申请Mapper
 * 提供退货申请的增删改查操作
 */
@Primary
public interface OmsOrderReturnApplyMapper {
    long countByExample(OmsOrderReturnApplyExample example);

    int deleteByExample(OmsOrderReturnApplyExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OmsOrderReturnApply row);

    int insertSelective(OmsOrderReturnApply row);

    List<OmsOrderReturnApply> selectByExample(OmsOrderReturnApplyExample example);

    OmsOrderReturnApply selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") OmsOrderReturnApply row, @Param("example") OmsOrderReturnApplyExample example);

    int updateByExample(@Param("row") OmsOrderReturnApply row, @Param("example") OmsOrderReturnApplyExample example);

    int updateByPrimaryKeySelective(OmsOrderReturnApply row);

    int updateByPrimaryKey(OmsOrderReturnApply row);
}
