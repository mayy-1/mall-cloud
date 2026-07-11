package com.mall.product.mapper;

import com.mall.product.model.PmsAlbumPic;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**相册图片Mapper */
public interface PmsAlbumPicMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PmsAlbumPic row);

    int insertSelective(PmsAlbumPic row);
    PmsAlbumPic selectByPrimaryKey(Long id);

    List<PmsAlbumPic> selectByCondition(PmsAlbumPic record);

    int deleteByCondition(PmsAlbumPic record);

    int updateSelectiveByCondition(@Param("record") PmsAlbumPic record, @Param("condition") PmsAlbumPic condition);

    int updateByPrimaryKeySelective(PmsAlbumPic row);

    int updateByPrimaryKey(PmsAlbumPic row);
}