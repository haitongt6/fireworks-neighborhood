package com.fireworks.api.controller;

import com.fireworks.common.api.ApiMemberDetails;
import com.fireworks.common.api.Result;
import com.fireworks.model.dto.MockPayParam;
import com.fireworks.service.OmsPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端支付接口。
 */
@RestController
@RequestMapping("/api/pay")
@Api(tags = "C端-支付")
public class ApiPayController {

    private final OmsPayService payService;

    public ApiPayController(OmsPayService payService) {
        this.payService = payService;
    }

    @PostMapping("/mock")
    @ApiOperation(value = "模拟支付", notes = "requestNo 保证幂等，支付锁防并发")
    public Result<Void> mockPay(@Validated @RequestBody MockPayParam param) {
        payService.mockPay(currentUserId(), param);
        return Result.success();
    }

    private Long currentUserId() {
        ApiMemberDetails details = (ApiMemberDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return details.getMember().getId();
    }
}
