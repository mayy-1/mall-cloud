package com.mall.product.mapper;

import com.mall.product.model.PmsMemberPrice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义会员价格Mapper
 * Created by macro on 2018/4/26.
 */
public interface PmsMemberPriceMapperCustom extends PmsMemberPriceMapper {
    /**
     * 批量创建
     */
    int insertList(@Param("list") List<PmsMemberPrice> memberPriceList);
}
