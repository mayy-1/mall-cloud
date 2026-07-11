package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsSubjectComment;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 专题评论Mapper
 * 提供专题评论的增删改查操作
 */
public interface CmsSubjectCommentMapper {

    int deleteByPrimaryKey(Long id);

    int insert(CmsSubjectComment row);

    int insertSelective(CmsSubjectComment row);
    CmsSubjectComment selectByPrimaryKey(Long id);

    List<CmsSubjectComment> selectByCondition(CmsSubjectComment record);

    int deleteByCondition(CmsSubjectComment record);

    int updateSelectiveByCondition(@Param("record") CmsSubjectComment record, @Param("condition") CmsSubjectComment condition);

    int updateByPrimaryKeySelective(CmsSubjectComment row);

    int updateByPrimaryKey(CmsSubjectComment row);
}