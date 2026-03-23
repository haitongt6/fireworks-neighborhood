package com.fireworks.model.constant;

/**
 * 锁库存状态枚举。
 */
public enum StockLockStatusEnum {

    LOCKED(0, "已锁定"),
    DEDUCTED(1, "已扣减"),
    RELEASED(2, "已释放");

    private final Integer code;
    private final String desc;

    StockLockStatusEnum(Integer code, String desc) {
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
