package com.mall.order.mapper;

import com.mall.order.domain.dto.OmsOrderReturnApplyResult;
import com.mall.order.domain.dto.OmsReturnApplyQueryParam;
import com.mall.order.model.OmsOrderReturnApply;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 订单退货申请Mapper
 * 提供退货申请的增删改查操作
 */
@Primary
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

    /** 后台退货申请列表查询 */
    List<OmsOrderReturnApply> getList(OmsReturnApplyQueryParam queryParam);

    /** 后台退货申请详情 */
    OmsOrderReturnApplyResult getDetail(@Param("id") Long id);
}
