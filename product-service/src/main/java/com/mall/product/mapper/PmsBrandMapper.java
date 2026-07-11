package com.mall.product.mapper;

import com.mall.product.model.PmsBrand;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**品牌Mapper */
public interface PmsBrandMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsBrand row);

    int insertSelective(PmsBrand row);
    PmsBrand selectByPrimaryKey(Long id);

    List<PmsBrand> selectByCondition(PmsBrand record);

    int deleteByCondition(PmsBrand record);

    int updateSelectiveByCondition(@Param("record") PmsBrand record, @Param("condition") PmsBrand condition);

    int updateByPrimaryKeySelective(PmsBrand row);

    int updateByPrimaryKeyWithBLOBs(PmsBrand row);

    int updateByPrimaryKey(PmsBrand row);
}