package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsHomeNewProduct;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 首页新品Mapper
 * 提供首页新品推荐的增删改查操作
 */
public interface SmsHomeNewProductMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SmsHomeNewProduct row);

    int insertSelective(SmsHomeNewProduct row);
    SmsHomeNewProduct selectByPrimaryKey(Long id);

    List<SmsHomeNewProduct> selectByCondition(SmsHomeNewProduct record);

    int deleteByCondition(SmsHomeNewProduct record);

    int updateSelectiveByCondition(@Param("record") SmsHomeNewProduct record, @Param("condition") SmsHomeNewProduct condition);

    int updateByPrimaryKeySelective(SmsHomeNewProduct row);

    int updateByPrimaryKey(SmsHomeNewProduct row);
}