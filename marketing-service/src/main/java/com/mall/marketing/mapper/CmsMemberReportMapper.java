package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsMemberReport;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会员举报Mapper
 * 提供会员举报信息的增删改查操作
 */
public interface CmsMemberReportMapper {

    int insert(CmsMemberReport row);

    int insertSelective(CmsMemberReport row);

}