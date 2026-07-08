package com.mall.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.mym.mall.common.constant.AuthConstant;
import com.mym.mall.common.service.RedisService;
import com.mall.user.mapper.UmsResourceMapper;
import com.mall.user.model.UmsResource;
import com.mall.user.model.UmsResourceExample;
import com.mall.user.service.IResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 后台资源管理Service实现类
 * Created by macro on 2020/2/2.
 */
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements IResourceService {

    /** 资源Mapper */
    private final UmsResourceMapper resourceMapper;
    /** Redis服务 */
    private final RedisService redisService;

    /** 应用名称 */
    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public int create(UmsResource umsResource) {
        umsResource.setCreateTime(new Date());
        int count = resourceMapper.insert(umsResource);
        initPathResourceMap();
        return count;
    }

    @Override
    public int update(Long id, UmsResource umsResource) {
        umsResource.setId(id);
        int count = resourceMapper.updateByPrimaryKeySelective(umsResource);
        initPathResourceMap();
        return count;
    }

    @Override
    public UmsResource getItem(Long id) {
        return resourceMapper.selectByPrimaryKey(id);
    }

    @Override
    public int delete(Long id) {
        int count = resourceMapper.deleteByPrimaryKey(id);
        initPathResourceMap();
        return count;
    }

    @Override
    public List<UmsResource> list(Long categoryId, String nameKeyword, String urlKeyword, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        UmsResourceExample example = new UmsResourceExample();
        UmsResourceExample.Criteria criteria = example.createCriteria();
        if (categoryId != null) {
            criteria.andCategoryIdEqualTo(categoryId);
        }
        if (StrUtil.isNotEmpty(nameKeyword)) {
            criteria.andNameLike('%' + nameKeyword + '%');
        }
        if (StrUtil.isNotEmpty(urlKeyword)) {
            criteria.andUrlLike('%' + urlKeyword + '%');
        }
        return resourceMapper.selectByExample(example);
    }

    @Override
    public List<UmsResource> listAll() {
        return resourceMapper.selectByExample(new UmsResourceExample());
    }

    @Override
    public Map<String, String> initPathResourceMap() {
        Map<String, String> pathResourceMap = new TreeMap<>();
        List<UmsResource> resourceList = resourceMapper.selectByExample(new UmsResourceExample());
        for (UmsResource resource : resourceList) {
            pathResourceMap.put("/" + applicationName + resource.getUrl(), resource.getId() + ":" + resource.getName());
        }
        redisService.del(AuthConstant.PATH_RESOURCE_MAP);
        redisService.hSetAll(AuthConstant.PATH_RESOURCE_MAP, pathResourceMap);
        return pathResourceMap;
    }
}
