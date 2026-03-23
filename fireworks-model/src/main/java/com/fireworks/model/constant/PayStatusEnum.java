package com.fireworks.model.constant;

/**
 * 支付状态枚举。
 */
public enum PayStatusEnum {

    WAIT_PAY(0, "未支付"),
    PAYING(1, "支付中"),
    PAY_SUCCESS(2, "支付成功"),
    PAY_FAIL(3, "支付失败"),
    CLOSED(4, "已关闭");

    private final Integer code;
    private final String desc;

    PayStatusEnum(Integer code, String desc) {
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
