package com.mall.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.mym.mall.common.constant.AuthConstant;
import com.mym.mall.common.service.RedisService;
import com.mall.user.mapper.UmsResourceMapper;
import com.mall.user.model.UmsResource;
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
    public UmsResource getItem(Long id) {
        return resourceMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<UmsResource> list(Long categoryId, String nameKeyword, String urlKeyword, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        UmsResource query = new UmsResource();
        if (categoryId != null) {
            query.setCategoryId(categoryId);
        }
        if (StrUtil.isNotEmpty(nameKeyword)) {
            query.setName('%' + nameKeyword + '%');
        }
        if (StrUtil.isNotEmpty(urlKeyword)) {
            query.setUrl('%' + urlKeyword + '%');
        }
        return resourceMapper.selectByCondition(query);
    }

    @Override
    public List<UmsResource> listAll() {
        return resourceMapper.selectByCondition(null);
    }

    @Override
    public Map<String, String> initPathResourceMap() {
        Map<String, String> pathResourceMap = new TreeMap<>();
        List<UmsResource> resourceList = resourceMapper.selectByCondition(null);
        for (UmsResource resource : resourceList) {
            pathResourceMap.put("/" + applicationName + resource.getUrl(), resource.getId() + ":" + resource.getName());
        }
        redisService.del(AuthConstant.PATH_RESOURCE_MAP);
        redisService.hSetAll(AuthConstant.PATH_RESOURCE_MAP, pathResourceMap);
        return pathResourceMap;
    }
}
