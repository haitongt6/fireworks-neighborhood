package com.fireworks.api.controller;

import com.fireworks.common.api.Result;
import com.fireworks.service.SmsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信验证码接口（C 端，无需 Token）。
 * <p>
 * 供登录注册页发送验证码使用。
 * </p>
 */
@RestController
@RequestMapping("/api/sms")
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * 发送短信验证码。
     * <p>
     * 无需 Token，限流规则：60 秒内同号仅可发送一次，每日最多 10 次。
     * </p>
     *
     * @param phone 手机号（必填）
     * @return 成功时 data 为 null；限流/发送失败时返回对应错误信息
     */
    @PostMapping("/sendVerifyCode")
    public Result<Void> sendVerifyCode(@RequestParam String phone) {
        smsService.sendVerifyCode(phone);
        return Result.success();
    }
}
