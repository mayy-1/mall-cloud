package com.mall.product.mapper;

import com.mall.product.model.PmsAlbum;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**相册Mapper */
public interface PmsAlbumMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsAlbum row);

    int insertSelective(PmsAlbum row);
    PmsAlbum selectByPrimaryKey(Long id);

    List<PmsAlbum> selectByCondition(PmsAlbum record);

    int deleteByCondition(PmsAlbum record);

    int updateSelectiveByCondition(@Param("record") PmsAlbum record, @Param("condition") PmsAlbum condition);

    int updateByPrimaryKeySelective(PmsAlbum row);

    int updateByPrimaryKey(PmsAlbum row);
}