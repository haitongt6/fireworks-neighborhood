package com.fireworks.service;

import com.fireworks.model.dto.MockPayParam;

/**
 * 支付服务。
 */
public interface OmsPayService {

    /**
     * C 端模拟支付。
     */
    void mockPay(Long userId, MockPayParam param);

    /**
     * 后台模拟支付成功。
     */
    void adminMockPaySuccess(MockPayParam param);
}
