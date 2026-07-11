package com.mall.product.mapper;

import com.mall.product.model.PmsMemberPrice;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**会员价格Mapper */
@Primary
public interface PmsMemberPriceMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsMemberPrice row);

    int insertSelective(PmsMemberPrice row);
    PmsMemberPrice selectByPrimaryKey(Long id);

    List<PmsMemberPrice> selectByCondition(PmsMemberPrice record);

    int deleteByCondition(PmsMemberPrice record);

    int updateSelectiveByCondition(@Param("record") PmsMemberPrice record, @Param("condition") PmsMemberPrice condition);

    int updateByPrimaryKeySelective(PmsMemberPrice row);

    int updateByPrimaryKey(PmsMemberPrice row);
}
