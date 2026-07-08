package com.mall.trade.mapper;

import com.mall.trade.model.PmsSkuStock;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 商品SKU库存数据访问接口
 */
public interface PmsSkuStockMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PmsSkuStock row);

    int insertSelective(PmsSkuStock row);

    PmsSkuStock selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PmsSkuStock row);

    int updateByPrimaryKey(PmsSkuStock row);
}
