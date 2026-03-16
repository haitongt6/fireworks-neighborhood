package com.fireworks.model.constant;

public class RedisKeyConstant {

    public static final String USER_INFO_KEY = "user:info:";

    /** 短信验证码，auth:code:phone:{phone}，5 分钟有效 */
    public static final String AUTH_CODE_PHONE_PREFIX = "auth:code:phone:";

    /** 短信 60 秒间隔限制，sms:limit:interval:{phone} */
    public static final String SMS_LIMIT_INTERVAL_PREFIX = "sms:limit:interval:";

    /** 短信每日总量限制，sms:limit:daily:{phone} */
    public static final String SMS_LIMIT_DAILY_PREFIX = "sms:limit:daily:";
}
