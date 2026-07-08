package com.mall.marketing.mapper;

import com.mall.marketing.model.CmsSubject;
import com.mall.marketing.model.CmsSubjectExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 商品专题Mapper
 * 提供商品专题的增删改查操作
 */
public interface CmsSubjectMapper {
    long countByExample(CmsSubjectExample example);

    int deleteByExample(CmsSubjectExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CmsSubject row);

    int insertSelective(CmsSubject row);

    List<CmsSubject> selectByExampleWithBLOBs(CmsSubjectExample example);

    List<CmsSubject> selectByExample(CmsSubjectExample example);

    CmsSubject selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") CmsSubject row, @Param("example") CmsSubjectExample example);

    int updateByExampleWithBLOBs(@Param("row") CmsSubject row, @Param("example") CmsSubjectExample example);

    int updateByExample(@Param("row") CmsSubject row, @Param("example") CmsSubjectExample example);

    int updateByPrimaryKeySelective(CmsSubject row);

    int updateByPrimaryKeyWithBLOBs(CmsSubject row);

    int updateByPrimaryKey(CmsSubject row);
}