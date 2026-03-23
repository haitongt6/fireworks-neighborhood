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

    /** 订单确认 submitToken：order:submit:token:{userId}:{token} */
    public static final String ORDER_SUBMIT_TOKEN_PREFIX = "order:submit:token:";

    /** 订单提交锁：order:submit:lock:{userId} */
    public static final String ORDER_SUBMIT_LOCK_PREFIX = "order:submit:lock:";

    /** 订单支付锁：order:pay:lock:{orderNo} */
    public static final String ORDER_PAY_LOCK_PREFIX = "order:pay:lock:";

    /** 订单关闭锁：order:close:lock:{orderNo} */
    public static final String ORDER_CLOSE_LOCK_PREFIX = "order:close:lock:";

    /** 订单过期 ZSet：score=过期时间戳，member=orderNo */
    public static final String ORDER_EXPIRE_ZSET = "order:expire:zset";

    /** 订单号序列：order:seq:{yyyyMMdd} */
    public static final String ORDER_SEQ_PREFIX = "order:seq:";

    /** 支付单号序列：pay:seq:{yyyyMMdd} */
    public static final String PAY_SEQ_PREFIX = "pay:seq:";
}
