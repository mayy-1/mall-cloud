package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsTopicComment;
import com.mall.marketing.model.CmsTopicCommentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 话题评论Mapper
 * 提供话题评论的增删改查操作
 */
public interface CmsTopicCommentMapper {
    long countByExample(CmsTopicCommentExample example);

    int deleteByExample(CmsTopicCommentExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CmsTopicComment row);

    int insertSelective(CmsTopicComment row);

    List<CmsTopicComment> selectByExample(CmsTopicCommentExample example);

    CmsTopicComment selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") CmsTopicComment row, @Param("example") CmsTopicCommentExample example);

    int updateByExample(@Param("row") CmsTopicComment row, @Param("example") CmsTopicCommentExample example);

    int updateByPrimaryKeySelective(CmsTopicComment row);

    int updateByPrimaryKey(CmsTopicComment row);
}