package com.mall.product.mapper;

import com.mall.product.model.PmsFeightTemplate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**运费模板Mapper */
public interface PmsFeightTemplateMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsFeightTemplate row);

    int insertSelective(PmsFeightTemplate row);
    PmsFeightTemplate selectByPrimaryKey(Long id);

    List<PmsFeightTemplate> selectByCondition(PmsFeightTemplate record);

    int deleteByCondition(PmsFeightTemplate record);

    int updateSelectiveByCondition(@Param("record") PmsFeightTemplate record, @Param("condition") PmsFeightTemplate condition);

    int updateByPrimaryKeySelective(PmsFeightTemplate row);

    int updateByPrimaryKey(PmsFeightTemplate row);
}