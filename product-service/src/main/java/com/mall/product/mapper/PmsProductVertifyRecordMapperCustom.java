package com.mall.product.mapper;

import com.mall.product.model.PmsProductVertifyRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义商品审核日志管理Mapper
 * Created by macro on 2018/4/27.
 */
public interface PmsProductVertifyRecordMapperCustom extends PmsProductVertifyRecordMapper {
    /**
     * 批量创建
     */
    int insertList(@Param("list") List<PmsProductVertifyRecord> list);
}
