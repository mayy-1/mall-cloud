package com.mall.product.mapper;

import com.mall.product.model.PmsProductVertifyRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**商品审核记录Mapper */
@Primary
public interface PmsProductVertifyRecordMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProductVertifyRecord row);

    int insertSelective(PmsProductVertifyRecord row);
    PmsProductVertifyRecord selectByPrimaryKey(Long id);

    List<PmsProductVertifyRecord> selectByCondition(PmsProductVertifyRecord record);

    int deleteByCondition(PmsProductVertifyRecord record);

    int updateSelectiveByCondition(@Param("record") PmsProductVertifyRecord record, @Param("condition") PmsProductVertifyRecord condition);

    int updateByPrimaryKeySelective(PmsProductVertifyRecord row);

    int updateByPrimaryKey(PmsProductVertifyRecord row);
}
