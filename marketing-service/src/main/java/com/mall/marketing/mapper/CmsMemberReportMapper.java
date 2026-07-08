package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsMemberReport;
import com.mall.marketing.model.CmsMemberReportExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员举报Mapper
 * 提供会员举报信息的增删改查操作
 */
public interface CmsMemberReportMapper {
    long countByExample(CmsMemberReportExample example);

    int deleteByExample(CmsMemberReportExample example);

    int insert(CmsMemberReport row);

    int insertSelective(CmsMemberReport row);

    List<CmsMemberReport> selectByExample(CmsMemberReportExample example);

    int updateByExampleSelective(@Param("row") CmsMemberReport row, @Param("example") CmsMemberReportExample example);

    int updateByExample(@Param("row") CmsMemberReport row, @Param("example") CmsMemberReportExample example);
}