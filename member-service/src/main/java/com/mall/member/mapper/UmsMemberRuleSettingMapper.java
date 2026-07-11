package com.mall.member.mapper;

import com.mall.member.model.UmsMemberRuleSetting;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员规则设置数据访问接口
 */
public interface UmsMemberRuleSettingMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberRuleSetting row);

    int insertSelective(UmsMemberRuleSetting row);
    UmsMemberRuleSetting selectByPrimaryKey(Long id);

    List<UmsMemberRuleSetting> selectByCondition(UmsMemberRuleSetting record);

    int deleteByCondition(UmsMemberRuleSetting record);

    int updateSelectiveByCondition(@Param("record") UmsMemberRuleSetting record, @Param("condition") UmsMemberRuleSetting condition);

    int updateByPrimaryKeySelective(UmsMemberRuleSetting row);

    int updateByPrimaryKey(UmsMemberRuleSetting row);
}