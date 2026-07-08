package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsHelp;
import com.mall.marketing.model.CmsHelpExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 帮助内容Mapper
 * 提供帮助内容的增删改查操作
 */
public interface CmsHelpMapper {
    long countByExample(CmsHelpExample example);

    int deleteByExample(CmsHelpExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CmsHelp row);

    int insertSelective(CmsHelp row);

    List<CmsHelp> selectByExampleWithBLOBs(CmsHelpExample example);

    List<CmsHelp> selectByExample(CmsHelpExample example);

    CmsHelp selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") CmsHelp row, @Param("example") CmsHelpExample example);

    int updateByExampleWithBLOBs(@Param("row") CmsHelp row, @Param("example") CmsHelpExample example);

    int updateByExample(@Param("row") CmsHelp row, @Param("example") CmsHelpExample example);

    int updateByPrimaryKeySelective(CmsHelp row);

    int updateByPrimaryKeyWithBLOBs(CmsHelp row);

    int updateByPrimaryKey(CmsHelp row);
}