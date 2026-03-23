package com.fireworks.service;

import com.fireworks.model.dto.OmsOrderQueryParam;
import com.fireworks.model.dto.OrderCancelParam;
import com.fireworks.model.dto.OrderSubmitParam;
import com.fireworks.model.vo.OmsOrderDetailVO;
import com.fireworks.model.vo.OmsOrderListItemVO;
import com.fireworks.model.vo.OrderConfirmVO;
import com.fireworks.model.vo.OrderSubmitVO;
import com.fireworks.model.vo.PageResult;

import java.util.List;

/**
 * 订单服务。
 */
public interface OmsOrderService {

    /**
     * 订单确认页。
     *
     * @param productIds 仅结算的商品ID列表（可空，空则按全购物车）
     */
    OrderConfirmVO confirm(Long userId, List<Long> productIds);

    /**
     * 提交订单。
     */
    OrderSubmitVO submit(Long userId, OrderSubmitParam param);

    /**
     * C 端订单列表。
     */
    PageResult<OmsOrderListItemVO> list(Long userId, OmsOrderQueryParam param);

    /**
     * 后台订单列表。
     */
    PageResult<OmsOrderListItemVO> adminList(OmsOrderQueryParam param);

    /**
     * 订单详情。
     */
    OmsOrderDetailVO detail(Long userId, String orderNo);

    /**
     * 后台订单详情。
     */
    OmsOrderDetailVO adminDetail(String orderNo);

    /**
     * 用户取消订单。
     */
    void cancel(Long userId, OrderCancelParam param);

    /**
     * 后台关闭订单。
     */
    void adminClose(String orderNo);

    /**
     * 超时关闭订单。
     */
    void closeExpiredOrder(String orderNo);
}
