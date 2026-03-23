package com.fireworks.model.constant;

/**
 * 支付方式枚举。
 */
public enum PayTypeEnum {

    WECHAT(1, "微信支付"),
    ALIPAY(2, "支付宝"),
    MOCK(9, "模拟支付");

    private final Integer code;
    private final String desc;

    PayTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
