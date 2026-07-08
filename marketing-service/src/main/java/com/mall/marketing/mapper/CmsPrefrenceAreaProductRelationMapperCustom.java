package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsPrefrenceAreaProductRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义优选和商品关系操作Mapper
 * Created by macro on 2018/4/26.
 */
public interface CmsPrefrenceAreaProductRelationMapperCustom extends CmsPrefrenceAreaProductRelationMapper {
    /**
     * 批量创建
     */
    int insertList(@Param("list") List<CmsPrefrenceAreaProductRelation> prefrenceAreaProductRelationList);
}
