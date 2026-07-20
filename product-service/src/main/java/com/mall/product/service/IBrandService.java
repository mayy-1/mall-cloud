package com.mall.product.service;

import com.mall.product.domain.dto.PmsBrandParam;
import com.mall.product.model.PmsBrand;

import java.util.List;

/**
 * 商品品牌Service
 * 查询类接口用户端+管理端共用，增删改为管理端专用
 */
public interface IBrandService {

    List<PmsBrand> listAllBrand();

    int createBrand(PmsBrandParam pmsBrandParam);

    int updateBrand(Long id, PmsBrandParam pmsBrandParam);

    int deleteBrand(Long id);

    int deleteBrand(List<Long> ids);

    List<PmsBrand> listBrand(String keyword, int pageNum, int pageSize);

    PmsBrand getBrand(Long id);

    int updateShowStatus(List<Long> ids, Integer showStatus);

    int updateFactoryStatus(List<Long> ids, Integer factoryStatus);

    List<PmsBrand> listRecommendBrand(int pageNum, int pageSize);
}
