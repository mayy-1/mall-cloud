package com.mall.order.mapper;

import com.mall.order.model.OmsOrder;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 订单Mapper
 * 提供订单的增删改查操作
 */
@Primary
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

    /**
     * 批量关闭订单（status=4，仅限未删除订单）
     */
    int closeByIds(@Param("ids") List<Long> ids);

    /**
     * 批量逻辑删除订单（delete_status=1，仅限未删除订单）
     */
    int deleteByIds(@Param("ids") List<Long> ids);
}
