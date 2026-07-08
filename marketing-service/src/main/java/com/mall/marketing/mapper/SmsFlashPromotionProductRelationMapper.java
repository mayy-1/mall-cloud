package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsFlashPromotionProductRelation;
import com.mall.marketing.model.SmsFlashPromotionProductRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 限时购商品关联Mapper
 * 提供限时购与商品关联关系的增删改查操作
 */
@Primary
public interface SmsFlashPromotionProductRelationMapper {
    long countByExample(SmsFlashPromotionProductRelationExample example);

    int deleteByExample(SmsFlashPromotionProductRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsFlashPromotionProductRelation row);

    int insertSelective(SmsFlashPromotionProductRelation row);

    List<SmsFlashPromotionProductRelation> selectByExample(SmsFlashPromotionProductRelationExample example);

    SmsFlashPromotionProductRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SmsFlashPromotionProductRelation row, @Param("example") SmsFlashPromotionProductRelationExample example);

    int updateByExample(@Param("row") SmsFlashPromotionProductRelation row, @Param("example") SmsFlashPromotionProductRelationExample example);

    int updateByPrimaryKeySelective(SmsFlashPromotionProductRelation row);

    int updateByPrimaryKey(SmsFlashPromotionProductRelation row);
}
