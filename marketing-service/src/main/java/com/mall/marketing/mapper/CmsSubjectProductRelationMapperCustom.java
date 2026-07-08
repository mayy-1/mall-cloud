package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsSubjectProductRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义商品和专题关系操作Mapper
 * Created by macro on 2018/4/26.
 */
public interface CmsSubjectProductRelationMapperCustom extends CmsSubjectProductRelationMapper {
    /**
     * 批量创建
     */
    int insertList(@Param("list") List<CmsSubjectProductRelation> subjectProductRelationList);
}
