package com.mall.product.mapper;

import com.mall.product.model.PmsComment;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**商品评价Mapper */
public interface PmsCommentMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsComment row);

    int insertSelective(PmsComment row);
    PmsComment selectByPrimaryKey(Long id);

    List<PmsComment> selectByCondition(PmsComment record);

    int deleteByCondition(PmsComment record);

    int updateSelectiveByCondition(@Param("record") PmsComment record, @Param("condition") PmsComment condition);

    int updateByPrimaryKeySelective(PmsComment row);

    int updateByPrimaryKeyWithBLOBs(PmsComment row);

    int updateByPrimaryKey(PmsComment row);
}