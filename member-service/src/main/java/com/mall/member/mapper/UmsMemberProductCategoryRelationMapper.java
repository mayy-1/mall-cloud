package com.mall.member.mapper;

import com.mall.member.model.UmsMemberProductCategoryRelation;
import com.mall.member.model.UmsMemberProductCategoryRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员与商品分类关联数据访问接口
 */
public interface UmsMemberProductCategoryRelationMapper {
    long countByExample(UmsMemberProductCategoryRelationExample example);

    int deleteByExample(UmsMemberProductCategoryRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberProductCategoryRelation row);

    int insertSelective(UmsMemberProductCategoryRelation row);

    List<UmsMemberProductCategoryRelation> selectByExample(UmsMemberProductCategoryRelationExample example);

    UmsMemberProductCategoryRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") UmsMemberProductCategoryRelation row, @Param("example") UmsMemberProductCategoryRelationExample example);

    int updateByExample(@Param("row") UmsMemberProductCategoryRelation row, @Param("example") UmsMemberProductCategoryRelationExample example);

    int updateByPrimaryKeySelective(UmsMemberProductCategoryRelation row);

    int updateByPrimaryKey(UmsMemberProductCategoryRelation row);
}