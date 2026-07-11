package com.mall.product.mapper;

import com.mall.product.model.PmsCommentReplay;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**评价回复Mapper */
public interface PmsCommentReplayMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsCommentReplay row);

    int insertSelective(PmsCommentReplay row);
    PmsCommentReplay selectByPrimaryKey(Long id);

    List<PmsCommentReplay> selectByCondition(PmsCommentReplay record);

    int deleteByCondition(PmsCommentReplay record);

    int updateSelectiveByCondition(@Param("record") PmsCommentReplay record, @Param("condition") PmsCommentReplay condition);

    int updateByPrimaryKeySelective(PmsCommentReplay row);

    int updateByPrimaryKey(PmsCommentReplay row);
}