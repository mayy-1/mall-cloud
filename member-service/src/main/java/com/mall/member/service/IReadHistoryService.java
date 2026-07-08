package com.mall.member.service;

import com.mall.member.domain.MemberReadHistory;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 会员浏览记录管理Service
 * Created by macro on 2018/8/3.
 */
public interface IReadHistoryService {
    /** 创建浏览记录 */
    int create(MemberReadHistory memberReadHistory);

    /** 批量删除浏览记录 */
    int delete(List<String> ids);

    /** 分页获取浏览记录 */
    Page<MemberReadHistory> list(Integer pageNum, Integer pageSize);

    /** 清空浏览记录 */
    void clear();
}
