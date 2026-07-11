package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsHomeRecommendSubject;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 首页专题推荐Mapper
 * 提供首页专题推荐的增删改查操作
 */
public interface SmsHomeRecommendSubjectMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SmsHomeRecommendSubject row);

    int insertSelective(SmsHomeRecommendSubject row);
    SmsHomeRecommendSubject selectByPrimaryKey(Long id);

    List<SmsHomeRecommendSubject> selectByCondition(SmsHomeRecommendSubject record);

    int deleteByCondition(SmsHomeRecommendSubject record);

    int updateSelectiveByCondition(@Param("record") SmsHomeRecommendSubject record, @Param("condition") SmsHomeRecommendSubject condition);

    int updateByPrimaryKeySelective(SmsHomeRecommendSubject row);

    int updateByPrimaryKey(SmsHomeRecommendSubject row);
}