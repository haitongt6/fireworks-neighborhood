package com.fireworks.model.constant;

/**
 * 订单来源枚举。
 */
public enum OrderSourceTypeEnum {

    CART(1, "购物车下单"),
    BUY_NOW(2, "立即购买");

    private final Integer code;
    private final String desc;

    OrderSourceTypeEnum(Integer code, String desc) {
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
