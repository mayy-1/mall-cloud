package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsHomeRecommendProduct;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 首页人气推荐Mapper
 * 提供首页人气推荐商品的增删改查操作
 */
public interface SmsHomeRecommendProductMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SmsHomeRecommendProduct row);

    int insertSelective(SmsHomeRecommendProduct row);
    SmsHomeRecommendProduct selectByPrimaryKey(Long id);

    List<SmsHomeRecommendProduct> selectByCondition(SmsHomeRecommendProduct record);

    int deleteByCondition(SmsHomeRecommendProduct record);

    int updateSelectiveByCondition(@Param("record") SmsHomeRecommendProduct record, @Param("condition") SmsHomeRecommendProduct condition);

    int updateByPrimaryKeySelective(SmsHomeRecommendProduct row);

    int updateByPrimaryKey(SmsHomeRecommendProduct row);
}