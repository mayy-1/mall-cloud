package com.mall.member.mapper;

import com.mall.member.model.UmsMemberReceiveAddress;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员收货地址数据访问接口
 */
public interface UmsMemberReceiveAddressMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberReceiveAddress row);

    int insertSelective(UmsMemberReceiveAddress row);
    UmsMemberReceiveAddress selectByPrimaryKey(Long id);

    List<UmsMemberReceiveAddress> selectByCondition(UmsMemberReceiveAddress record);

    int deleteByCondition(UmsMemberReceiveAddress record);

    int updateSelectiveByCondition(@Param("record") UmsMemberReceiveAddress record, @Param("condition") UmsMemberReceiveAddress condition);

    int updateByPrimaryKeySelective(UmsMemberReceiveAddress row);

    int updateByPrimaryKey(UmsMemberReceiveAddress row);
}