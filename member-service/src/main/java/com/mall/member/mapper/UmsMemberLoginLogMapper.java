package com.mall.member.mapper;

import com.mall.member.model.UmsMemberLoginLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员登录日志数据访问接口
 */
public interface UmsMemberLoginLogMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberLoginLog row);

    int insertSelective(UmsMemberLoginLog row);
    UmsMemberLoginLog selectByPrimaryKey(Long id);

    List<UmsMemberLoginLog> selectByCondition(UmsMemberLoginLog record);

    int deleteByCondition(UmsMemberLoginLog record);

    int updateSelectiveByCondition(@Param("record") UmsMemberLoginLog record, @Param("condition") UmsMemberLoginLog condition);

    int updateByPrimaryKeySelective(UmsMemberLoginLog row);

    int updateByPrimaryKey(UmsMemberLoginLog row);
}