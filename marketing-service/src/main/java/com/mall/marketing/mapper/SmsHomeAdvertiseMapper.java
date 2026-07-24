package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsHomeAdvertise;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 首页广告Mapper
 * 提供首页轮播广告的增删改查操作
 */
public interface SmsHomeAdvertiseMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SmsHomeAdvertise row);

    int insertSelective(SmsHomeAdvertise row);
    SmsHomeAdvertise selectByPrimaryKey(Long id);

    List<SmsHomeAdvertise> selectByCondition(SmsHomeAdvertise record);

    int deleteByCondition(SmsHomeAdvertise record);

    int updateSelectiveByCondition(@Param("record") SmsHomeAdvertise record, @Param("condition") SmsHomeAdvertise condition);

    int updateByPrimaryKeySelective(SmsHomeAdvertise row);

    int updateByPrimaryKey(SmsHomeAdvertise row);

    List<SmsHomeAdvertise> selectHomeAdvertises(@Param("now") Date now);
}