package com.mall.member.mapper;

import com.mall.member.model.UmsMemberProductCategoryRelation;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员与商品分类关联数据访问接口
 */
public interface UmsMemberProductCategoryRelationMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberProductCategoryRelation row);

    int insertSelective(UmsMemberProductCategoryRelation row);
    UmsMemberProductCategoryRelation selectByPrimaryKey(Long id);

    List<UmsMemberProductCategoryRelation> selectByCondition(UmsMemberProductCategoryRelation record);

    int deleteByCondition(UmsMemberProductCategoryRelation record);

    int updateSelectiveByCondition(@Param("record") UmsMemberProductCategoryRelation record, @Param("condition") UmsMemberProductCategoryRelation condition);

    int updateByPrimaryKeySelective(UmsMemberProductCategoryRelation row);

    int updateByPrimaryKey(UmsMemberProductCategoryRelation row);
}