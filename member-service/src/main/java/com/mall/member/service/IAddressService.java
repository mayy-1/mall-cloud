package com.mall.member.service;

import com.mall.member.model.UmsMemberReceiveAddress;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户地址管理Service
 * Created by macro on 2018/8/28.
 */
public interface IAddressService {
    /** 添加收货地址 */
    int add(UmsMemberReceiveAddress address);

    /** 删除收货地址 */
    int delete(Long id);

    /** 修改收货地址（事务操作） */
    @Transactional
    int update(Long id, UmsMemberReceiveAddress address);

    /** 获取当前用户所有收货地址 */
    List<UmsMemberReceiveAddress> list();

    /** 根据ID获取收货地址详情 */
    UmsMemberReceiveAddress getItem(Long id);
}
