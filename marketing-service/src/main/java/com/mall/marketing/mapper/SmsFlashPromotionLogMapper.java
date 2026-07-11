package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsFlashPromotionLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 限时购活动日志Mapper
 * 提供限时购活动日志的增删改查操作
 */
public interface SmsFlashPromotionLogMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(SmsFlashPromotionLog row);

    int insertSelective(SmsFlashPromotionLog row);
    SmsFlashPromotionLog selectByPrimaryKey(Integer id);

    List<SmsFlashPromotionLog> selectByCondition(SmsFlashPromotionLog record);

    int deleteByCondition(SmsFlashPromotionLog record);

    int updateSelectiveByCondition(@Param("record") SmsFlashPromotionLog record, @Param("condition") SmsFlashPromotionLog condition);

    int updateByPrimaryKeySelective(SmsFlashPromotionLog row);

    int updateByPrimaryKey(SmsFlashPromotionLog row);
}