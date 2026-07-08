package com.mall.marketing.service.impl;

import com.mall.marketing.mapper.CmsPrefrenceAreaMapper;
import com.mall.marketing.model.CmsPrefrenceArea;
import com.mall.marketing.model.CmsPrefrenceAreaExample;
import com.mall.marketing.service.IPrefrenceAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品优选管理Service实现类
 * Created by macro on 2018/6/1.
 */
@Service
@RequiredArgsConstructor
public class PrefrenceAreaServiceImpl implements IPrefrenceAreaService {
    /** 商品优选Mapper */
    private final CmsPrefrenceAreaMapper prefrenceAreaMapper;

    @Override
    public List<CmsPrefrenceArea> listAll() {
        return prefrenceAreaMapper.selectByExample(new CmsPrefrenceAreaExample());
    }
}
