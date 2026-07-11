package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsSubject;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 商品专题Mapper
 * 提供商品专题的增删改查操作
 */
public interface CmsSubjectMapper {

    int deleteByPrimaryKey(Long id);

    int insert(CmsSubject row);

    int insertSelective(CmsSubject row);
    CmsSubject selectByPrimaryKey(Long id);

    List<CmsSubject> selectByCondition(CmsSubject record);

    int deleteByCondition(CmsSubject record);

    int updateSelectiveByCondition(@Param("record") CmsSubject record, @Param("condition") CmsSubject condition);

    int updateByPrimaryKeySelective(CmsSubject row);

    int updateByPrimaryKeyWithBLOBs(CmsSubject row);

    int updateByPrimaryKey(CmsSubject row);
}