package com.mall.order.mapper;

import com.mall.order.model.OmsCompanyAddress;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 收货地址Mapper
 * 提供收货地址的增删改查操作
 */
public interface OmsCompanyAddressMapper {

    int deleteByPrimaryKey(Long id);

    int insert(OmsCompanyAddress row);

    int insertSelective(OmsCompanyAddress row);
    OmsCompanyAddress selectByPrimaryKey(Long id);

    List<OmsCompanyAddress> selectByCondition(OmsCompanyAddress record);

    int deleteByCondition(OmsCompanyAddress record);

    int updateSelectiveByCondition(@Param("record") OmsCompanyAddress record, @Param("condition") OmsCompanyAddress condition);

    int updateByPrimaryKeySelective(OmsCompanyAddress row);

    int updateByPrimaryKey(OmsCompanyAddress row);
}