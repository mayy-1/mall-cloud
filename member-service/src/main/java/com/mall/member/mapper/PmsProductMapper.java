package com.mall.member.mapper;

import com.mall.member.model.PmsProduct;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 商品信息数据访问接口
 */
public interface PmsProductMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProduct row);

    int insertSelective(PmsProduct row);

    PmsProduct selectByPrimaryKey(Long id);

    List<PmsProduct> selectByCondition(PmsProduct record);

    int deleteByCondition(PmsProduct record);

    int updateSelectiveByCondition(@Param("record") PmsProduct record, @Param("condition") PmsProduct condition);

    int updateByPrimaryKeySelective(PmsProduct row);

    int updateByPrimaryKeyWithBLOBs(PmsProduct row);

    int updateByPrimaryKey(PmsProduct row);
}