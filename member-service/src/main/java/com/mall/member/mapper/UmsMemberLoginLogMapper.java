package com.mall.member.mapper;

import com.mall.member.model.UmsMemberLoginLog;
import com.mall.member.model.UmsMemberLoginLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员登录日志数据访问接口
 */
public interface UmsMemberLoginLogMapper {
    long countByExample(UmsMemberLoginLogExample example);

    int deleteByExample(UmsMemberLoginLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberLoginLog row);

    int insertSelective(UmsMemberLoginLog row);

    List<UmsMemberLoginLog> selectByExample(UmsMemberLoginLogExample example);

    UmsMemberLoginLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") UmsMemberLoginLog row, @Param("example") UmsMemberLoginLogExample example);

    int updateByExample(@Param("row") UmsMemberLoginLog row, @Param("example") UmsMemberLoginLogExample example);

    int updateByPrimaryKeySelective(UmsMemberLoginLog row);

    int updateByPrimaryKey(UmsMemberLoginLog row);
}