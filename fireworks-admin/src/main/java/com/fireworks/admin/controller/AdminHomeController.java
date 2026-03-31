package com.fireworks.admin.controller;

import com.fireworks.common.api.Result;
import com.fireworks.model.dto.AdminHomeQueryParam;
import com.fireworks.model.vo.AdminHomeStatisticsVO;
import com.fireworks.service.AdminHomeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台首页统计接口。
 */
@RestController
@RequestMapping("/admin/home")
@Api(tags = "后台-首页统计")
public class AdminHomeController {

    private final AdminHomeService adminHomeService;

    public AdminHomeController(AdminHomeService adminHomeService) {
        this.adminHomeService = adminHomeService;
    }

    @GetMapping("/statistics")
    @ApiOperation(value = "获取首页统计数据")
    public Result<AdminHomeStatisticsVO> getStatistics(AdminHomeQueryParam param) {
        return Result.success(adminHomeService.getStatistics(param));
    }
}
