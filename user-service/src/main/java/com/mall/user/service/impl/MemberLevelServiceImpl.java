package com.mall.user.service.impl;

import com.mall.user.mapper.UmsMemberLevelMapper;
import com.mall.user.model.UmsMemberLevel;
import com.mall.user.service.IMemberLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会员等级管理Service实现类
 * Created by macro on 2018/4/26.
 */
@Service
@RequiredArgsConstructor
public class MemberLevelServiceImpl implements IMemberLevelService {

    /** 会员等级Mapper */
    private final UmsMemberLevelMapper memberLevelMapper;

    @Override
    public List<UmsMemberLevel> list(Integer defaultStatus) {
        UmsMemberLevel query = new UmsMemberLevel();
        query.setDefaultStatus(defaultStatus);
        return memberLevelMapper.selectByCondition(query);
    }
}
