package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsHomeRecommendProduct;
import com.mall.marketing.model.SmsHomeRecommendProductExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 首页人气推荐Mapper
 * 提供首页人气推荐商品的增删改查操作
 */
public interface SmsHomeRecommendProductMapper {
    long countByExample(SmsHomeRecommendProductExample example);

    int deleteByExample(SmsHomeRecommendProductExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsHomeRecommendProduct row);

    int insertSelective(SmsHomeRecommendProduct row);

    List<SmsHomeRecommendProduct> selectByExample(SmsHomeRecommendProductExample example);

    SmsHomeRecommendProduct selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SmsHomeRecommendProduct row, @Param("example") SmsHomeRecommendProductExample example);

    int updateByExample(@Param("row") SmsHomeRecommendProduct row, @Param("example") SmsHomeRecommendProductExample example);

    int updateByPrimaryKeySelective(SmsHomeRecommendProduct row);

    int updateByPrimaryKey(SmsHomeRecommendProduct row);
}