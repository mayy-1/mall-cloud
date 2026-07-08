package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsSubjectComment;
import com.mall.marketing.model.CmsSubjectCommentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 专题评论Mapper
 * 提供专题评论的增删改查操作
 */
public interface CmsSubjectCommentMapper {
    long countByExample(CmsSubjectCommentExample example);

    int deleteByExample(CmsSubjectCommentExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CmsSubjectComment row);

    int insertSelective(CmsSubjectComment row);

    List<CmsSubjectComment> selectByExample(CmsSubjectCommentExample example);

    CmsSubjectComment selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") CmsSubjectComment row, @Param("example") CmsSubjectCommentExample example);

    int updateByExample(@Param("row") CmsSubjectComment row, @Param("example") CmsSubjectCommentExample example);

    int updateByPrimaryKeySelective(CmsSubjectComment row);

    int updateByPrimaryKey(CmsSubjectComment row);
}