package com.fireworks.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 后台首页统计查询参数。
 */
@ApiModel(value = "AdminHomeQueryParam", description = "后台首页统计查询参数")
public class AdminHomeQueryParam {

    @ApiModelProperty(value = "开始时间，格式 yyyy-MM-dd", example = "2026-03-01")
    private String startTime;

    @ApiModelProperty(value = "结束时间，格式 yyyy-MM-dd", example = "2026-03-30")
    private String endTime;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
