package com.mall.member.service.impl;

import com.mall.member.mapper.UmsMemberReceiveAddressMapper;
import com.mall.member.model.UmsMember;
import com.mall.member.model.UmsMemberReceiveAddress;import com.mall.member.service.IAddressService;
import com.mall.member.service.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 用户地址管理Service实现类
 * Created by macro on 2018/8/28.
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements IAddressService {
    /** 会员服务 */
    private final IMemberService memberService;
    /** 收货地址Mapper */
    private final UmsMemberReceiveAddressMapper addressMapper;

    @Override
    public int add(UmsMemberReceiveAddress address) {
        UmsMember currentMember = memberService.getCurrentMember();
        address.setMemberId(currentMember.getId());
        return addressMapper.insert(address);
    }

    @Override
    public int delete(Long id) {
        UmsMember currentMember = memberService.getCurrentMember();
        UmsMemberReceiveAddress condition = new UmsMemberReceiveAddress();
        condition.setMemberId(currentMember.getId());
        condition.setId(id);
        return addressMapper.deleteByCondition(condition);
    }

    @Override
    public int update(Long id, UmsMemberReceiveAddress address) {
        address.setId(null);
        UmsMember currentMember = memberService.getCurrentMember();
        UmsMemberReceiveAddress condition = new UmsMemberReceiveAddress();
        condition.setMemberId(currentMember.getId());
        condition.setId(id);
        if(address.getDefaultStatus()==1){
            //先将原来的默认地址去除
            UmsMemberReceiveAddress record= new UmsMemberReceiveAddress();
            record.setDefaultStatus(0);
            UmsMemberReceiveAddress updateCondition = new UmsMemberReceiveAddress();
            updateCondition.setMemberId(currentMember.getId());
            updateCondition.setDefaultStatus(1);
            addressMapper.updateSelectiveByCondition(record,updateCondition);
        }
        return addressMapper.updateSelectiveByCondition(address,condition);
    }

    @Override
    public List<UmsMemberReceiveAddress> list() {
        UmsMember currentMember = memberService.getCurrentMember();
        UmsMemberReceiveAddress condition = new UmsMemberReceiveAddress();
        condition.setMemberId(currentMember.getId());
        return addressMapper.selectByCondition(condition);
    }

    @Override
    public UmsMemberReceiveAddress getItem(Long id) {
        UmsMember currentMember = memberService.getCurrentMember();
        UmsMemberReceiveAddress condition = new UmsMemberReceiveAddress();
        condition.setMemberId(currentMember.getId());
        condition.setId(id);
        List<UmsMemberReceiveAddress> addressList = addressMapper.selectByCondition(condition);
        if(!CollectionUtils.isEmpty(addressList)){
            return addressList.get(0);
        }
        return null;
    }
}
