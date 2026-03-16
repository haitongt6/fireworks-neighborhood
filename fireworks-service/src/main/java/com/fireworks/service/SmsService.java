package com.fireworks.service;

/**
 * 短信服务接口。
 * <p>
 * 负责验证码发送、限流校验。限流通过 Redis Lua 脚本原子执行。
 * </p>
 */
public interface SmsService {

    /**
     * 发送短信验证码。
     * <p>
     * 限流规则（Lua 原子执行）：
     * <ul>
     *   <li>60 秒内同手机号仅可发送一次</li>
     *   <li>同一手机号每日最多 10 次</li>
     * </ul>
     * </p>
     *
     * @param phone 手机号
     * @throws IllegalArgumentException 手机号格式非法
     * @throws SmsLimitException        限流触发，见 {@link SmsLimitException#getCode()}
     * @throws SmsSendException         发送失败
     */
    void sendVerifyCode(String phone);
}
