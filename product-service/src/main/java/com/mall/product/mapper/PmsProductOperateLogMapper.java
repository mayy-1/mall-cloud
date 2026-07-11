package com.mall.product.mapper;

import com.mall.product.model.PmsProductOperateLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**商品操作日志Mapper */
public interface PmsProductOperateLogMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsProductOperateLog row);

    int insertSelective(PmsProductOperateLog row);
    PmsProductOperateLog selectByPrimaryKey(Long id);

    List<PmsProductOperateLog> selectByCondition(PmsProductOperateLog record);

    int deleteByCondition(PmsProductOperateLog record);

    int updateSelectiveByCondition(@Param("record") PmsProductOperateLog record, @Param("condition") PmsProductOperateLog condition);

    int updateByPrimaryKeySelective(PmsProductOperateLog row);

    int updateByPrimaryKey(PmsProductOperateLog row);
}