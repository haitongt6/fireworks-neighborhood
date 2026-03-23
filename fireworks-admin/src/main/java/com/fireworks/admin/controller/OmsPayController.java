package com.fireworks.admin.controller;

import com.fireworks.common.api.Result;
import com.fireworks.model.constant.PermissionConstant;
import com.fireworks.model.dto.MockPayParam;
import com.fireworks.service.OmsPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台支付管理接口。
 */
@RestController
@RequestMapping("/admin/pay")
@Api(tags = "后台-支付管理")
public class OmsPayController {

    private final OmsPayService payService;

    public OmsPayController(OmsPayService payService) {
        this.payService = payService;
    }

    @PostMapping("/mock/success")
    @ApiOperation(value = "后台模拟支付成功", notes = "requestNo 保证幂等")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).PAY_MOCK_SUCCESS)")
    public Result<Void> mockSuccess(@Validated @RequestBody MockPayParam param) {
        payService.adminMockPaySuccess(param);
        return Result.success();
    }
}
