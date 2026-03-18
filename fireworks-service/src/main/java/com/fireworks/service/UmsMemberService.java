package com.fireworks.service;

import com.fireworks.model.pojo.UmsMember;
import com.fireworks.model.vo.MemberLoginVO;

/**
 * C 端会员服务接口。
 */
public interface UmsMemberService {

    /**
     * 手机号 + 验证码登录。
     * <p>
     * 首次登录的手机号自动注册，验证码一次性消费。
     * </p>
     *
     * @param phone      手机号
     * @param verifyCode 短信验证码
     * @return JWT Token + 会员信息
     * @throws IllegalArgumentException 验证码错误/过期、账号被禁用
     */
    MemberLoginVO loginByPhone(String phone, String verifyCode);

    /**
     * 根据手机号查询会员。
     *
     * @return 会员实体；不存在时返回 null
     */
    UmsMember getByPhone(String phone);
}
