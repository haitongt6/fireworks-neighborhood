package com.fireworks.model.constant;

/**
 * 订单确认接口（GET /order/confirm）业务错误码，与 C 端约定一致。
 */
public final class OrderConfirmApiCode {

    /** 购物车为空，无法进入确认页 */
    public static final int CART_EMPTY = 4001;

    /** 传入的结算商品 ID 与当前购物车无交集 */
    public static final int SELECTION_NOT_IN_CART = 4002;

    private OrderConfirmApiCode() {
    }
}
