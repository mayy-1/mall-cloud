package com.mall.product.mapper;

import com.mall.product.model.PmsAlbum;
import com.mall.product.model.PmsAlbumExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**相册Mapper */
public interface PmsAlbumMapper {
    long countByExample(PmsAlbumExample example);

    int deleteByExample(PmsAlbumExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PmsAlbum row);

    int insertSelective(PmsAlbum row);

    List<PmsAlbum> selectByExample(PmsAlbumExample example);

    PmsAlbum selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") PmsAlbum row, @Param("example") PmsAlbumExample example);

    int updateByExample(@Param("row") PmsAlbum row, @Param("example") PmsAlbumExample example);

    int updateByPrimaryKeySelective(PmsAlbum row);

    int updateByPrimaryKey(PmsAlbum row);
}