package com.fireworks.model.constant;

/**
 * 订单状态枚举。
 */
public enum OrderStatusEnum {

    WAIT_PAY(0, "待支付"),
    PAID(1, "已支付"),
    CANCELED(2, "已取消"),
    CLOSED(3, "已关闭"),
    FINISHED(4, "已完成");

    private final Integer code;
    private final String desc;

    OrderStatusEnum(Integer code, String desc) {
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
