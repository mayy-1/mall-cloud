package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsTopicComment;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 话题评论Mapper
 * 提供话题评论的增删改查操作
 */
public interface CmsTopicCommentMapper {

    int deleteByPrimaryKey(Long id);

    int insert(CmsTopicComment row);

    int insertSelective(CmsTopicComment row);
    CmsTopicComment selectByPrimaryKey(Long id);

    List<CmsTopicComment> selectByCondition(CmsTopicComment record);

    int deleteByCondition(CmsTopicComment record);

    int updateSelectiveByCondition(@Param("record") CmsTopicComment record, @Param("condition") CmsTopicComment condition);

    int updateByPrimaryKeySelective(CmsTopicComment row);

    int updateByPrimaryKey(CmsTopicComment row);
}