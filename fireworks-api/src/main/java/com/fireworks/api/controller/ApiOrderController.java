package com.fireworks.api.controller;

import com.fireworks.common.api.ApiMemberDetails;
import com.fireworks.common.api.Result;
import com.fireworks.model.dto.OmsOrderQueryParam;
import com.fireworks.model.dto.OrderCancelParam;
import com.fireworks.model.dto.OrderSubmitParam;
import com.fireworks.model.vo.OmsOrderDetailVO;
import com.fireworks.model.vo.OmsOrderListItemVO;
import com.fireworks.model.vo.OrderConfirmVO;
import com.fireworks.model.vo.OrderSubmitVO;
import com.fireworks.model.vo.PageResult;
import com.fireworks.service.OmsOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * C 端订单接口。
 */
@RestController
@RequestMapping("/api/order")
@Api(tags = "C端-订单")
public class ApiOrderController {

    private final OmsOrderService orderService;

    public ApiOrderController(OmsOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/confirm")
    @ApiOperation(value = "订单确认页", notes = "从购物车读取有效商品，校验库存/限购，生成 submitToken")
    public Result<OrderConfirmVO> confirm(
            @ApiParam(value = "仅结算商品ID列表，逗号分隔") @RequestParam(required = false) List<Long> productIds) {
        return Result.success(orderService.confirm(currentUserId(),
                productIds == null ? Collections.<Long>emptyList() : productIds));
    }

    @PostMapping("/submit")
    @ApiOperation(value = "提交订单", notes = "提交幂等（submitToken）+ 用户级锁，事务内完成下单全流程")
    public Result<OrderSubmitVO> submit(@Validated @RequestBody OrderSubmitParam param) {
        return Result.success(orderService.submit(currentUserId(), param));
    }

    @GetMapping("/list")
    @ApiOperation(value = "C端订单列表")
    public Result<PageResult<OmsOrderListItemVO>> list(OmsOrderQueryParam param) {
        return Result.success(orderService.list(currentUserId(), param));
    }

    @GetMapping("/detail")
    @ApiOperation(value = "C端订单详情")
    public Result<OmsOrderDetailVO> detail(
            @ApiParam(value = "订单号", required = true) @RequestParam String orderNo) {
        return Result.success(orderService.detail(currentUserId(), orderNo));
    }

    @PostMapping("/cancel")
    @ApiOperation(value = "取消订单", notes = "仅限待支付状态可取消，释放锁库存")
    public Result<Void> cancel(@Validated @RequestBody OrderCancelParam param) {
        orderService.cancel(currentUserId(), param);
        return Result.success();
    }

    private Long currentUserId() {
        ApiMemberDetails details = (ApiMemberDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return details.getMember().getId();
    }
}
