package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsHomeNewProduct;
import com.mall.marketing.model.SmsHomeNewProductExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 首页新品Mapper
 * 提供首页新品推荐的增删改查操作
 */
public interface SmsHomeNewProductMapper {
    long countByExample(SmsHomeNewProductExample example);

    int deleteByExample(SmsHomeNewProductExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsHomeNewProduct row);

    int insertSelective(SmsHomeNewProduct row);

    List<SmsHomeNewProduct> selectByExample(SmsHomeNewProductExample example);

    SmsHomeNewProduct selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SmsHomeNewProduct row, @Param("example") SmsHomeNewProductExample example);

    int updateByExample(@Param("row") SmsHomeNewProduct row, @Param("example") SmsHomeNewProductExample example);

    int updateByPrimaryKeySelective(SmsHomeNewProduct row);

    int updateByPrimaryKey(SmsHomeNewProduct row);
}