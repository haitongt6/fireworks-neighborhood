package com.fireworks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fireworks.common.api.ApiMemberDetails;
import com.fireworks.model.constant.RedisKeyConstant;
import com.fireworks.model.pojo.UmsMember;
import com.fireworks.model.vo.MemberLoginVO;
import com.fireworks.service.UmsMemberService;
import com.fireworks.service.mapper.UmsMemberMapper;
import com.fireworks.service.utils.JwtTokenUtil;
import com.fireworks.service.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UmsMemberServiceImpl implements UmsMemberService {

    private static final Logger log = LoggerFactory.getLogger(UmsMemberServiceImpl.class);

    private final UmsMemberMapper memberMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.tokenHead:Bearer }")
    private String tokenHead;

    public UmsMemberServiceImpl(UmsMemberMapper memberMapper,
                                StringRedisTemplate stringRedisTemplate,
                                JwtTokenUtil jwtTokenUtil) {
        this.memberMapper = memberMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public MemberLoginVO loginByPhone(String phone, String verifyCode) {
        String codeKey = RedisKeyConstant.AUTH_CODE_PHONE_PREFIX + phone;
        String savedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (savedCode == null) {
            throw new IllegalArgumentException("验证码已过期，请重新获取");
        }
        if (!savedCode.equals(verifyCode)) {
            throw new IllegalArgumentException("验证码错误");
        }

        UmsMember member = getByPhone(phone);
        if (member == null) {
            member = new UmsMember();
            member.setPhone(phone);
            member.setNickname("用户" + phone.substring(phone.length() - 4));
            member.setStatus(1);
            member.setCreateTime(new Date());
            member.setUpdateTime(new Date());
            memberMapper.insert(member);
            log.info("C 端新用户自动注册: phone={}", maskPhone(phone));
        }

        if (member.getStatus() == null || member.getStatus() != 1) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        String token = jwtTokenUtil.generateToken(phone);

        ApiMemberDetails memberDetails = new ApiMemberDetails(member);
        RedisUtil.set(RedisKeyConstant.API_MEMBER_INFO_KEY + phone,
                memberDetails, 30, TimeUnit.MINUTES);

        stringRedisTemplate.delete(codeKey);

        MemberLoginVO vo = new MemberLoginVO();
        vo.setToken(token);
        vo.setTokenHead(tokenHead);
        vo.setMember(member);
        log.info("C 端用户登录成功: phone={}", maskPhone(phone));
        return vo;
    }

    @Override
    public UmsMember getByPhone(String phone) {
        QueryWrapper<UmsMember> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        return memberMapper.selectOne(wrapper);
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
