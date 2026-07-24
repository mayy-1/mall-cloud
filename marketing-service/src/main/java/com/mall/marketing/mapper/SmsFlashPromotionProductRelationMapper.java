package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsFlashPromotionProductRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 限时购商品关联Mapper
 * 提供限时购与商品关联关系的增删改查操作
 */
@Primary
public interface SmsFlashPromotionProductRelationMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SmsFlashPromotionProductRelation row);

    int insertSelective(SmsFlashPromotionProductRelation row);
    SmsFlashPromotionProductRelation selectByPrimaryKey(Long id);

    List<SmsFlashPromotionProductRelation> selectByCondition(SmsFlashPromotionProductRelation record);

    int deleteByCondition(SmsFlashPromotionProductRelation record);

    int updateSelectiveByCondition(@Param("record") SmsFlashPromotionProductRelation record, @Param("condition") SmsFlashPromotionProductRelation condition);

    int updateByPrimaryKeySelective(SmsFlashPromotionProductRelation row);

    int updateByPrimaryKey(SmsFlashPromotionProductRelation row);

    List<SmsFlashPromotionProductRelation> getList(@Param("flashPromotionId") Long flashPromotionId,
                                                    @Param("flashPromotionSessionId") Long flashPromotionSessionId);
}
