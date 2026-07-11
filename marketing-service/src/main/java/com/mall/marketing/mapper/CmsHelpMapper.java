package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsHelp;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 帮助内容Mapper
 * 提供帮助内容的增删改查操作
 */
public interface CmsHelpMapper {

    int deleteByPrimaryKey(Long id);

    int insert(CmsHelp row);

    int insertSelective(CmsHelp row);
    CmsHelp selectByPrimaryKey(Long id);

    List<CmsHelp> selectByCondition(CmsHelp record);

    int deleteByCondition(CmsHelp record);

    int updateSelectiveByCondition(@Param("record") CmsHelp record, @Param("condition") CmsHelp condition);

    int updateByPrimaryKeySelective(CmsHelp row);

    int updateByPrimaryKeyWithBLOBs(CmsHelp row);

    int updateByPrimaryKey(CmsHelp row);
}