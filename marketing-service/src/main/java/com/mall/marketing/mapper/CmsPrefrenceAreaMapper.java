package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsPrefrenceArea;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 商品优选Mapper
 * 提供商品优选区域的增删改查操作
 */
public interface CmsPrefrenceAreaMapper {

    int deleteByPrimaryKey(Long id);

    int insert(CmsPrefrenceArea row);

    int insertSelective(CmsPrefrenceArea row);
    CmsPrefrenceArea selectByPrimaryKey(Long id);

    List<CmsPrefrenceArea> selectByCondition(CmsPrefrenceArea record);

    int deleteByCondition(CmsPrefrenceArea record);

    int updateSelectiveByCondition(@Param("record") CmsPrefrenceArea record, @Param("condition") CmsPrefrenceArea condition);

    int updateByPrimaryKeySelective(CmsPrefrenceArea row);

    int updateByPrimaryKeyWithBLOBs(CmsPrefrenceArea row);

    int updateByPrimaryKey(CmsPrefrenceArea row);
}