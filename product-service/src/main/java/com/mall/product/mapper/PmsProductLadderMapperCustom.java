package com.mall.product.mapper;

import com.mall.product.model.PmsProductLadder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义会员阶梯价格Mapper
 * Created by macro on 2018/4/26.
 */
public interface PmsProductLadderMapperCustom extends PmsProductLadderMapper {
    /**
     * 批量创建
     */
    int insertList(@Param("list") List<PmsProductLadder> productLadderList);
}
