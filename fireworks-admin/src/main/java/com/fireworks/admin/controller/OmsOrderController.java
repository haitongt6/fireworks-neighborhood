package com.fireworks.admin.controller;

import com.fireworks.common.api.Result;
import com.fireworks.model.constant.PermissionConstant;
import com.fireworks.model.dto.OmsOrderQueryParam;
import com.fireworks.model.vo.OmsOrderDetailVO;
import com.fireworks.model.vo.OmsOrderListItemVO;
import com.fireworks.model.vo.PageResult;
import com.fireworks.service.OmsOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台订单管理接口。
 */
@RestController
@RequestMapping("/admin/order")
@Api(tags = "后台-订单管理")
public class OmsOrderController {

    private final OmsOrderService orderService;

    public OmsOrderController(OmsOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/list")
    @ApiOperation(value = "后台订单列表")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ORDER_LIST)")
    public Result<PageResult<OmsOrderListItemVO>> list(OmsOrderQueryParam param) {
        return Result.success(orderService.adminList(param));
    }

    @GetMapping("/detail")
    @ApiOperation(value = "后台订单详情")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ORDER_DETAIL)")
    public Result<OmsOrderDetailVO> detail(
            @ApiParam(value = "订单号", required = true) @RequestParam String orderNo) {
        return Result.success(orderService.adminDetail(orderNo));
    }

    @PostMapping("/close")
    @ApiOperation(value = "关闭订单", notes = "仅限待支付状态可关闭，释放锁库存")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ORDER_CLOSE)")
    public Result<Void> close(
            @ApiParam(value = "订单号", required = true) @RequestParam String orderNo) {
        orderService.adminClose(orderNo);
        return Result.success();
    }
}
