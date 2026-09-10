package com.mall.product.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.github.pagehelper.PageHelper;
import com.mall.product.domain.dto.PmsBrandParam;
import com.mall.product.mapper.PmsBrandMapper;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.model.PmsBrand;
import com.mall.product.model.PmsProduct;
import com.mall.product.service.IBrandService;
import com.mall.product.util.PinyinUtil;
import com.mym.mall.common.service.RedisService;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品品牌Service实现类
 * 【管理端+用户端】品牌查询为共用（listAllBrand/listBrand/listRecommendBrand），
 * 品牌增删改、显示状态/厂家状态切换为管理端专用
 * Created by macro on 2018/4/26.
 */
@Service
@RequiredArgsConstructor
public class IBrandServiceImpl implements IBrandService {

    /** 品牌Mapper */
    private final PmsBrandMapper brandMapper;

    private final PmsProductMapper productMapper;

    /** Redis缓存服务 */
    private final RedisService redisService;
    /** Redis数据库前缀 */
    @Value("${redis.database}")
    private String REDIS_DATABASE;
    /** 品牌缓存过期时间（秒） */
    @Value("${redis.expire.brand}")
    private long REDIS_EXPIRE_BRAND;
    /** 品牌缓存key前缀 */
    @Value("${redis.key.brand}")
    private String REDIS_KEY_BRAND;

    @Override
    public List<PmsBrand> listAllBrand() {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_BRAND + ":all";
        Object cached = redisService.get(key);
        if (cached != null) {
            return (List<PmsBrand>) cached;
        }
        List<PmsBrand> list = brandMapper.selectByCondition(new PmsBrand());
        redisService.set(key, list, REDIS_EXPIRE_BRAND);
        return list;
    }

    /** 删除所有品牌缓存 */
    private void deleteBrandCache() {
        String prefix = REDIS_DATABASE + ":" + REDIS_KEY_BRAND + ":";
        redisService.delByPrefix(prefix);
    }

    @Override
    @Transactional
    public int createBrand(PmsBrandParam pmsBrandParam) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtils.copyProperties(pmsBrandParam, pmsBrand);
        //如果创建时首字母为空，取名称的第一个为首字母
        if (StringUtils.isEmpty(pmsBrand.getFirstLetter())) {
            String brandName = pmsBrand.getName();
            String firstLetter = PinyinUtil.getFirstLetter(brandName);
            pmsBrand.setFirstLetter(firstLetter);
        }
        int count = brandMapper.insertSelective(pmsBrand);
        deleteBrandCache();
        return count;
    }

    @Override
    @Transactional
    public int updateBrand(Long id, PmsBrandParam pmsBrandParam) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtils.copyProperties(pmsBrandParam, pmsBrand);
        pmsBrand.setId(id);
        //如果创建时首字母为空，取名称的第一个为首字母
        if (StringUtils.isEmpty(pmsBrand.getFirstLetter())) {
            String brandName = pmsBrand.getName();
            String firstLetter = PinyinUtil.getFirstLetter(brandName);
            pmsBrand.setFirstLetter(firstLetter);
        }
        //更新品牌时要更新商品中的品牌名称
        PmsProduct product = new PmsProduct();
        product.setBrandName(pmsBrand.getName());
        PmsProduct condition = new PmsProduct();
        condition.setBrandId(id);
        productMapper.updateSelectiveByCondition(product, condition);
        int count = brandMapper.updateByPrimaryKeySelective(pmsBrand);
        deleteBrandCache();
        return count;
    }

    @Override
    public int deleteBrand(Long id) {
        PmsProduct pmsProduct = new PmsProduct();
        pmsProduct.setBrandId(id);
        List<PmsProduct> pmsProductList = productMapper.selectByCondition(pmsProduct);
        if (CollectionUtils.isNotEmpty(pmsProductList)) {
            return 0;
        }
        int count = brandMapper.deleteByPrimaryKey(id);
        deleteBrandCache();
        return count;
    }

    @Override
    public int deleteBrand(List<Long> ids) {
        PmsProduct pmsProduct = new PmsProduct();
        List<PmsProduct> pmsProductList = new ArrayList<>();
        for (Long id : ids) {
            pmsProduct.setBrandId(id);
            pmsProductList.addAll(productMapper.selectByCondition(pmsProduct));
        }
        if (CollectionUtils.isNotEmpty(pmsProductList)) {
            return 0;
        }
        int count = 0;
        for (Long id : ids) {
            count += brandMapper.deleteByPrimaryKey(id);
        }
        deleteBrandCache();
        return count;
    }

    @Override
    public List<PmsBrand> listBrand(String keyword, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("sort desc");
        PmsBrand condition = new PmsBrand();
        if (!StringUtils.isEmpty(keyword)) {
            condition.setName("%" + keyword + "%");
        }
        return brandMapper.selectByCondition(condition);
    }

    @Override
    public PmsBrand getBrand(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateShowStatus(List<Long> ids, Integer showStatus) {
        int count = 0;
        for (Long id : ids) {
            PmsBrand brand = new PmsBrand();
            brand.setId(id);
            brand.setShowStatus(showStatus);
            count += brandMapper.updateByPrimaryKeySelective(brand);
        }
        deleteBrandCache();
        return count;
    }

    @Override
    public int updateFactoryStatus(List<Long> ids, Integer factoryStatus) {
        int count = 0;
        for (Long id : ids) {
            PmsBrand brand = new PmsBrand();
            brand.setId(id);
            brand.setFactoryStatus(factoryStatus);
            count += brandMapper.updateByPrimaryKeySelective(brand);
        }
        deleteBrandCache();
        return count;
    }

    @Override
    public List<PmsBrand> listRecommendBrand(int pageNum, int pageSize) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_BRAND + ":recommend:" + pageNum + ":" + pageSize;
        Object cached = redisService.get(key);
        if (cached != null) {
            return (List<PmsBrand>) cached;
        }
        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("sort desc");
        PmsBrand condition = new PmsBrand();
        condition.setShowStatus(1);
        List<PmsBrand> list = brandMapper.selectByCondition(condition);
        redisService.set(key, list, REDIS_EXPIRE_BRAND);
        return list;
    }
}
