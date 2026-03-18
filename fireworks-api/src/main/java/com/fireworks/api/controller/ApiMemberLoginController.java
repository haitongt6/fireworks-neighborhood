package com.fireworks.api.controller;

import com.fireworks.common.api.ApiMemberDetails;
import com.fireworks.common.api.Result;
import com.fireworks.model.constant.RedisKeyConstant;
import com.fireworks.model.dto.MemberLoginParam;
import com.fireworks.model.pojo.UmsMember;
import com.fireworks.model.vo.MemberLoginVO;
import com.fireworks.service.UmsMemberService;
import com.fireworks.service.utils.RedisUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * C 端会员登录相关接口。
 */
@RestController
@RequestMapping("/api/member")
public class ApiMemberLoginController {

    private final UmsMemberService memberService;

    public ApiMemberLoginController(UmsMemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 手机号 + 验证码登录（新用户自动注册）。
     */
    @PostMapping("/loginByPhone")
    public Result<MemberLoginVO> loginByPhone(@Validated @RequestBody MemberLoginParam param) {
        MemberLoginVO vo = memberService.loginByPhone(param.getPhone(), param.getVerifyCode());
        return Result.success(vo);
    }

    /**
     * 获取当前登录会员信息。
     */
    @GetMapping("/info")
    public Result<UmsMember> info() {
        return Result.success(getCurrentMember());
    }

    /**
     * 登出（清除 Redis 会话）。
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        UmsMember member = getCurrentMember();
        RedisUtil.delete(RedisKeyConstant.API_MEMBER_INFO_KEY + member.getPhone());
        return Result.success();
    }

    private UmsMember getCurrentMember() {
        ApiMemberDetails details = (ApiMemberDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return details.getMember();
    }
}
