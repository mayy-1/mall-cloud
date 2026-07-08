package com.mall.marketing.service;

import com.mall.marketing.model.CmsPrefrenceArea;

import java.util.List;

/**
 * 商品优选管理Service
 * Created by macro on 2018/6/1.
 */
public interface IPrefrenceAreaService {
    /**
     * 获取所有优选专区
     */
    List<CmsPrefrenceArea> listAll();
}
