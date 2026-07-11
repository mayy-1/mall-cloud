package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsFlashPromotion;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 限时购活动Mapper
 * 提供限时购活动的增删改查操作
 */
public interface SmsFlashPromotionMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SmsFlashPromotion row);

    int insertSelective(SmsFlashPromotion row);
    SmsFlashPromotion selectByPrimaryKey(Long id);

    List<SmsFlashPromotion> selectByCondition(SmsFlashPromotion record);

    int deleteByCondition(SmsFlashPromotion record);

    int updateSelectiveByCondition(@Param("record") SmsFlashPromotion record, @Param("condition") SmsFlashPromotion condition);

    int updateByPrimaryKeySelective(SmsFlashPromotion row);

    int updateByPrimaryKey(SmsFlashPromotion row);
}