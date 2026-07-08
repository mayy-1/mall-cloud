package com.mall.order.mapper;

import com.mall.order.model.OmsCompanyAddress;
import com.mall.order.model.OmsCompanyAddressExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 收货地址Mapper
 * 提供收货地址的增删改查操作
 */
public interface OmsCompanyAddressMapper {
    long countByExample(OmsCompanyAddressExample example);

    int deleteByExample(OmsCompanyAddressExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OmsCompanyAddress row);

    int insertSelective(OmsCompanyAddress row);

    List<OmsCompanyAddress> selectByExample(OmsCompanyAddressExample example);

    OmsCompanyAddress selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") OmsCompanyAddress row, @Param("example") OmsCompanyAddressExample example);

    int updateByExample(@Param("row") OmsCompanyAddress row, @Param("example") OmsCompanyAddressExample example);

    int updateByPrimaryKeySelective(OmsCompanyAddress row);

    int updateByPrimaryKey(OmsCompanyAddress row);
}