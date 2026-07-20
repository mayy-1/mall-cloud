package com.mall.member.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.mall.api.client.user.UserMemberLevelClient;
import com.mall.api.dto.MemberLevelDTO;
import com.mym.mall.common.constant.AuthConstant;
import com.mym.mall.common.dto.UserDto;
import com.mym.mall.common.exception.Asserts;
import com.mall.member.mapper.UmsIntegrationChangeHistoryMapper;
import com.mall.member.mapper.UmsMemberLoginLogMapper;
import com.mall.member.mapper.UmsMemberMapper;
import com.mall.member.model.UmsIntegrationChangeHistory;
import com.mall.member.model.UmsMember;
import com.mall.member.model.UmsMemberLoginLog;
import com.mall.member.service.IMemberCacheService;
import com.mall.member.service.IMemberService;
import com.mall.member.util.StpMemberUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 会员管理Service实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements IMemberService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberServiceImpl.class);
    /** 会员Mapper */
    private final UmsMemberMapper memberMapper;
    /** 积分变动历史Mapper */
    private final UmsIntegrationChangeHistoryMapper integrationChangeHistoryMapper;
    /** 登录日志Mapper */
    private final UmsMemberLoginLogMapper loginLogMapper;
    /** 会员等级 Feign 调用 */
    private final UserMemberLevelClient userMemberLevelClient;
    /** 会员缓存服务 */
    private final IMemberCacheService memberCacheService;
    /** Redis验证码Key前缀 */
    @Value("${redis.key.authCode}")
    private String REDIS_KEY_PREFIX_AUTH_CODE;
    /** 验证码过期时间(秒) */
    @Value("${redis.expire.authCode}")
    private Long AUTH_CODE_EXPIRE_SECONDS;

    @Override
    public UmsMember getByUsername(String username) {
        UmsMember example = new UmsMember();
        example.setUsername(username);
        List<UmsMember> memberList = memberMapper.selectByCondition(example);
        if (!CollectionUtils.isEmpty(memberList)) {
            return memberList.get(0);
        }
        return null;
    }

    @Override
    public UmsMember getById(Long id) {
        return memberMapper.selectByPrimaryKey(id);
    }

    @Override
    public void register(String username, String password, String telephone, String authCode) {
        //验证验证码
        if(!verifyAuthCode(authCode,telephone)){
            Asserts.fail("验证码错误");
        }
        //查询是否已有该用户
        UmsMember example = new UmsMember();
        example.setUsername(username);
        example.setPhone(telephone);
        List<UmsMember> umsMembers = memberMapper.selectByCondition(example);
        if (!CollectionUtils.isEmpty(umsMembers)) {
            Asserts.fail("该用户已经存在");
        }
        //没有该用户进行添加操作
        UmsMember umsMember = new UmsMember();
        umsMember.setUsername(username);
        umsMember.setPhone(telephone);
        umsMember.setPassword(BCrypt.hashpw(password));
        umsMember.setCreateTime(new Date());
        umsMember.setStatus(1);
        //获取默认会员等级并设置（通过 Feign 调用 user-service）
        List<MemberLevelDTO> memberLevelList = userMemberLevelClient.listByDefaultStatus(1).getData();
        if (!CollectionUtils.isEmpty(memberLevelList)) {
            umsMember.setMemberLevelId(memberLevelList.get(0).getId());
        }
        memberMapper.insert(umsMember);
        umsMember.setPassword(null);
    }

    @Override
    public void generateAuthCode(String telephone) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i=0;i<6;i++){
            sb.append(random.nextInt(10));
        }
        memberCacheService.setAuthCode(telephone,sb.toString());
        log.info("手机号：{}，验证码：{}",telephone,sb);
    }

    @Override
    public void updatePassword(String telephone, String password, String authCode) {
        UmsMember example = new UmsMember();
        example.setPhone(telephone);
        List<UmsMember> memberList = memberMapper.selectByCondition(example);
        if(CollectionUtils.isEmpty(memberList)){
            Asserts.fail("该账号不存在");
        }
        //验证验证码
        if(!verifyAuthCode(authCode,telephone)){
            Asserts.fail("验证码错误");
        }
        UmsMember umsMember = memberList.get(0);
        umsMember.setPassword(BCrypt.hashpw(password));
        memberMapper.updateByPrimaryKeySelective(umsMember);
        memberCacheService.delMember(umsMember.getId());
    }

    @Override
    public UmsMember getCurrentMember() {
        UserDto userDto = (UserDto) StpMemberUtil.getSession().get(AuthConstant.STP_MEMBER_INFO);
        UmsMember member = memberCacheService.getMember(userDto.getId());
        if(member!=null){
            return member;
        }else{
            member = getById(userDto.getId());
            memberCacheService.setMember(member);
            return member;
        }
    }

    @Override
    public void updateIntegration(Long id, Integer integration, Integer sourceType) {
        // 查询旧积分，计算变动量
        UmsMember member = memberMapper.selectByPrimaryKey(id);
        int oldIntegration = member.getIntegration() == null ? 0 : member.getIntegration();
        int changeCount = integration - oldIntegration;

        // 更新积分
        UmsMember record = new UmsMember();
        record.setId(id);
        record.setIntegration(integration);
        memberMapper.updateByPrimaryKeySelective(record);

        // 记录积分变动历史
        UmsIntegrationChangeHistory history = new UmsIntegrationChangeHistory();
        history.setMemberId(id);
        history.setCreateTime(new Date());
        history.setChangeType(changeCount > 0 ? 0 : 1);  // 0=增加，1=减少
        history.setChangeCount(Math.abs(changeCount));
        history.setSourceType(sourceType);
        history.setOperateNote(String.format("积分从 %d 变为 %d，%s %d",
                oldIntegration, integration,
                changeCount > 0 ? "增加" : "减少",
                Math.abs(changeCount)));
        integrationChangeHistoryMapper.insert(history);

        memberCacheService.delMember(id);
    }

    @Override
    public SaTokenInfo login(String username, String password) {
        if(StrUtil.isEmpty(username)||StrUtil.isEmpty(password)){
            Asserts.fail("用户名或密码不能为空！");
        }
        UmsMember member = getByUsername(username);
        if(member==null){
            Asserts.fail("找不到该用户！");
        }
        if (!BCrypt.checkpw(password, member.getPassword())) {
            Asserts.fail("密码不正确！");
        }
        if(member.getStatus()!=1){
            Asserts.fail("该账号已被禁用！");
        }
        // 登录校验成功后，一行代码实现登录
        StpMemberUtil.login(member.getId());
        UserDto userDto = new UserDto();
        userDto.setId(member.getId());
        userDto.setUsername(member.getUsername());
        userDto.setClientId(AuthConstant.PORTAL_CLIENT_ID);
        // 将用户信息存储到Session中
        StpMemberUtil.getSession().set(AuthConstant.STP_MEMBER_INFO,userDto);
        // 记录登录日志
        insertLoginLog(member);
        // 获取当前登录用户Token信息
        return StpMemberUtil.getTokenInfo();
    }

    @Override
    public void logout() {
        //先清空缓存
        UserDto userDto = (UserDto) StpMemberUtil.getSession().get(AuthConstant.STP_MEMBER_INFO);
        memberCacheService.delMember(userDto.getId());
        //再调用sa-token的登出方法
        StpMemberUtil.logout();
    }

    /**
     * 插入会员登录日志
     */
    private void insertLoginLog(UmsMember member) {
        UmsMemberLoginLog loginLog = new UmsMemberLoginLog();
        loginLog.setMemberId(member.getId());
        loginLog.setCreateTime(new Date());
        loginLog.setLoginType(0);  // 0=PC
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            loginLog.setIp(request.getRemoteAddr());
        }
        loginLogMapper.insert(loginLog);
    }

    //对输入的验证码进行校验
    private boolean verifyAuthCode(String authCode, String telephone){
        if(StringUtils.isEmpty(authCode)){
            return false;
        }
        String realAuthCode = memberCacheService.getAuthCode(telephone);
        return authCode.equals(realAuthCode);
    }

}
