package com.mall.member.mapper;

import com.mall.member.model.UmsMemberStatisticsInfo;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员统计信息数据访问接口
 */
public interface UmsMemberStatisticsInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberStatisticsInfo row);

    int insertSelective(UmsMemberStatisticsInfo row);
    UmsMemberStatisticsInfo selectByPrimaryKey(Long id);

    List<UmsMemberStatisticsInfo> selectByCondition(UmsMemberStatisticsInfo record);

    int deleteByCondition(UmsMemberStatisticsInfo record);

    int updateSelectiveByCondition(@Param("record") UmsMemberStatisticsInfo record, @Param("condition") UmsMemberStatisticsInfo condition);

    int updateByPrimaryKeySelective(UmsMemberStatisticsInfo row);

    int updateByPrimaryKey(UmsMemberStatisticsInfo row);
}