package com.fireworks.model.constant;

/**
 * 订单操作类型枚举。
 */
public enum OrderOperateTypeEnum {

    SUBMIT(1, "提交订单"),
    PAY(2, "发起支付"),
    PAY_SUCCESS(3, "支付成功"),
    PAY_FAIL(4, "支付失败"),
    USER_CANCEL(5, "用户取消订单"),
    TIMEOUT_CLOSE(6, "超时关闭订单"),
    RELEASE_STOCK(7, "释放锁库存"),
    ADMIN_CLOSE(8, "后台关闭订单");

    private final Integer code;
    private final String desc;

    OrderOperateTypeEnum(Integer code, String desc) {
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
