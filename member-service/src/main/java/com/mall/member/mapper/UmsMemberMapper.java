package com.mall.member.mapper;

import com.mall.member.model.UmsMember;
import com.mall.member.model.UmsMemberExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员数据访问接口
 */
public interface UmsMemberMapper {
    long countByExample(UmsMemberExample example);

    int deleteByExample(UmsMemberExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UmsMember row);

    int insertSelective(UmsMember row);

    List<UmsMember> selectByExample(UmsMemberExample example);

    UmsMember selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") UmsMember row, @Param("example") UmsMemberExample example);

    int updateByExample(@Param("row") UmsMember row, @Param("example") UmsMemberExample example);

    int updateByPrimaryKeySelective(UmsMember row);

    int updateByPrimaryKey(UmsMember row);
}