package com.mall.member.service;

import com.mall.member.domain.MemberBrandAttention;
import org.springframework.data.domain.Page;

/**
 * 会员关注Service
 * Created by macro on 2018/8/2.
 */
public interface IAttentionService {
    /** 添加品牌关注 */
    int add(MemberBrandAttention memberBrandAttention);

    /** 取消品牌关注 */
    int delete(Long brandId);

    /** 分页获取关注品牌列表 */
    Page<MemberBrandAttention> list(Integer pageNum, Integer pageSize);

    /** 获取关注品牌详情 */
    MemberBrandAttention detail(Long brandId);

    /** 清空关注列表 */
    void clear();
}
