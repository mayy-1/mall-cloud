package com.mall.member.mapper;

import com.mall.member.model.UmsMemberRuleSetting;
import com.mall.member.model.UmsMemberRuleSettingExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员规则设置数据访问接口
 */
public interface UmsMemberRuleSettingMapper {
    long countByExample(UmsMemberRuleSettingExample example);

    int deleteByExample(UmsMemberRuleSettingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberRuleSetting row);

    int insertSelective(UmsMemberRuleSetting row);

    List<UmsMemberRuleSetting> selectByExample(UmsMemberRuleSettingExample example);

    UmsMemberRuleSetting selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") UmsMemberRuleSetting row, @Param("example") UmsMemberRuleSettingExample example);

    int updateByExample(@Param("row") UmsMemberRuleSetting row, @Param("example") UmsMemberRuleSettingExample example);

    int updateByPrimaryKeySelective(UmsMemberRuleSetting row);

    int updateByPrimaryKey(UmsMemberRuleSetting row);
}