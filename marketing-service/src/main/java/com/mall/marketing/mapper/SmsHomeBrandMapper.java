package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsHomeBrand;
import com.mall.marketing.model.SmsHomeBrandExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 首页品牌Mapper
 * 提供首页推荐品牌的增删改查操作
 */
public interface SmsHomeBrandMapper {
    long countByExample(SmsHomeBrandExample example);

    int deleteByExample(SmsHomeBrandExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsHomeBrand row);

    int insertSelective(SmsHomeBrand row);

    List<SmsHomeBrand> selectByExample(SmsHomeBrandExample example);

    SmsHomeBrand selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SmsHomeBrand row, @Param("example") SmsHomeBrandExample example);

    int updateByExample(@Param("row") SmsHomeBrand row, @Param("example") SmsHomeBrandExample example);

    int updateByPrimaryKeySelective(SmsHomeBrand row);

    int updateByPrimaryKey(SmsHomeBrand row);
}