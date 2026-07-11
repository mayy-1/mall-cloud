package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsHomeBrand;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 首页品牌Mapper
 * 提供首页推荐品牌的增删改查操作
 */
public interface SmsHomeBrandMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SmsHomeBrand row);

    int insertSelective(SmsHomeBrand row);
    SmsHomeBrand selectByPrimaryKey(Long id);

    List<SmsHomeBrand> selectByCondition(SmsHomeBrand record);

    int deleteByCondition(SmsHomeBrand record);

    int updateSelectiveByCondition(@Param("record") SmsHomeBrand record, @Param("condition") SmsHomeBrand condition);

    int updateByPrimaryKeySelective(SmsHomeBrand row);

    int updateByPrimaryKey(SmsHomeBrand row);
}