package com.mall.member.service;

import com.mall.member.domain.MemberProductCollection;
import org.springframework.data.domain.Page;

/**
 * 会员收藏Service
 * Created by macro on 2018/8/2.
 */
public interface IProductCollectionService {
    /** 添加商品收藏 */
    int add(MemberProductCollection productCollection);

    /** 删除商品收藏 */
    int delete(Long productId);

    /** 分页获取收藏列表 */
    Page<MemberProductCollection> list(Integer pageNum, Integer pageSize);

    /** 获取收藏商品详情 */
    MemberProductCollection detail(Long productId);

    /** 清空收藏列表 */
    void clear();
}
