package com.mall.marketing.mapper;

import com.mall.marketing.model.SmsHomeAdvertise;
import com.mall.marketing.model.SmsHomeAdvertiseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 首页广告Mapper
 * 提供首页轮播广告的增删改查操作
 */
public interface SmsHomeAdvertiseMapper {
    long countByExample(SmsHomeAdvertiseExample example);

    int deleteByExample(SmsHomeAdvertiseExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsHomeAdvertise row);

    int insertSelective(SmsHomeAdvertise row);

    List<SmsHomeAdvertise> selectByExample(SmsHomeAdvertiseExample example);

    SmsHomeAdvertise selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SmsHomeAdvertise row, @Param("example") SmsHomeAdvertiseExample example);

    int updateByExample(@Param("row") SmsHomeAdvertise row, @Param("example") SmsHomeAdvertiseExample example);

    int updateByPrimaryKeySelective(SmsHomeAdvertise row);

    int updateByPrimaryKey(SmsHomeAdvertise row);
}