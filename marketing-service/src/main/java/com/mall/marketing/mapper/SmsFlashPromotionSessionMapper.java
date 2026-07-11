package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsFlashPromotionSession;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 限时购场次Mapper
 * 提供限时购场次的增删改查操作
 */
public interface SmsFlashPromotionSessionMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SmsFlashPromotionSession row);

    int insertSelective(SmsFlashPromotionSession row);
    SmsFlashPromotionSession selectByPrimaryKey(Long id);

    List<SmsFlashPromotionSession> selectByCondition(SmsFlashPromotionSession record);

    int deleteByCondition(SmsFlashPromotionSession record);

    int updateSelectiveByCondition(@Param("record") SmsFlashPromotionSession record, @Param("condition") SmsFlashPromotionSession condition);

    int updateByPrimaryKeySelective(SmsFlashPromotionSession row);

    int updateByPrimaryKey(SmsFlashPromotionSession row);
}