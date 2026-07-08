package com.mall.marketing.mapper;

import com.mall.marketing.domain.dto.SmsFlashPromotionProduct;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义限时购商品关系管理Mapper
 * Created by macro on 2018/11/16.
 */
public interface SmsFlashPromotionProductRelationMapperCustom extends SmsFlashPromotionProductRelationMapper {
    /**
     * 获取限时购及相关商品信息
     */
    List<SmsFlashPromotionProduct> getList(@Param("flashPromotionId") Long flashPromotionId, @Param("flashPromotionSessionId") Long flashPromotionSessionId);
}
