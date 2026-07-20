package com.mall.product.mapper;

import com.mall.product.model.PmsMemberPrice;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

/**
 * 会员价格Mapper
 * 【管理端专用】商品创建/编辑时设置不同会员等级的价格
 * 对应表: pms_member_price
 */
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

    /**
     * 批量插入会员价格
     */
    int insertList(@Param("list") List<PmsMemberPrice> list);
}
