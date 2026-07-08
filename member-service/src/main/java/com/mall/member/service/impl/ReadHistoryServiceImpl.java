package com.mall.member.service.impl;

import com.mall.member.domain.MemberReadHistory;
import com.mall.member.model.UmsMember;
import com.mall.member.repository.MemberReadHistoryRepository;
import com.mall.member.service.IReadHistoryService;
import com.mall.member.service.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 会员浏览记录管理Service实现类
 * Created by macro on 2018/8/3.
 */
@Service
@RequiredArgsConstructor
public class ReadHistoryServiceImpl implements IReadHistoryService {
    /** 浏览历史MongoDB Repository */
    private final MemberReadHistoryRepository memberReadHistoryRepository;
    /** 会员服务 */
    private final IMemberService memberService;

    @Override
    public int create(MemberReadHistory memberReadHistory) {
        UmsMember member = memberService.getCurrentMember();
        memberReadHistory.setMemberId(member.getId());
        memberReadHistory.setMemberNickname(member.getNickname());
        memberReadHistory.setMemberIcon(member.getIcon());
        memberReadHistory.setId(null);
        memberReadHistory.setCreateTime(new Date());
        memberReadHistoryRepository.save(memberReadHistory);
        return 1;
    }

    @Override
    public int delete(List<String> ids) {
        List<MemberReadHistory> deleteList = new ArrayList<>();
        for(String id:ids){
            MemberReadHistory memberReadHistory = new MemberReadHistory();
            memberReadHistory.setId(id);
            deleteList.add(memberReadHistory);
        }
        memberReadHistoryRepository.deleteAll(deleteList);
        return ids.size();
    }

    @Override
    public Page<MemberReadHistory> list(Integer pageNum, Integer pageSize) {
        UmsMember member = memberService.getCurrentMember();
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);
        return memberReadHistoryRepository.findByMemberIdOrderByCreateTimeDesc(member.getId(),pageable);
    }

    @Override
    public void clear() {
        UmsMember member = memberService.getCurrentMember();
        memberReadHistoryRepository.deleteAllByMemberId(member.getId());
    }
}
