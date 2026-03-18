package com.fireworks.model.constant;

public class RedisKeyConstant {

    public static final String USER_INFO_KEY = "user:info:";

    /** C 端分类列表缓存，api:category:list */
    public static final String API_CATEGORY_LIST = "api:category:list";

    /** 短信验证码，auth:code:phone:{phone}，5 分钟有效 */
    public static final String AUTH_CODE_PHONE_PREFIX = "auth:code:phone:";

    /** 短信 60 秒间隔限制，sms:limit:interval:{phone} */
    public static final String SMS_LIMIT_INTERVAL_PREFIX = "sms:limit:interval:";

    /** 短信每日总量限制，sms:limit:daily:{phone} */
    public static final String SMS_LIMIT_DAILY_PREFIX = "sms:limit:daily:";

    /** C 端会员登录会话，api:member:info:{phone}，滑动窗口 30 分钟 */
    public static final String API_MEMBER_INFO_KEY = "api:member:info:";

    /** 购物车 Hash，cart:{userId}，当天 23:59:59 过期 */
    public static final String CART_KEY_PREFIX = "cart:";
}
