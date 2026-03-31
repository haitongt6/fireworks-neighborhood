package com.fireworks.service;

import com.fireworks.model.dto.AdminHomeQueryParam;
import com.fireworks.model.vo.AdminHomeStatisticsVO;

/**
 * 后台首页统计服务。
 */
public interface AdminHomeService {

    /**
     * 获取后台首页统计数据。
     */
    AdminHomeStatisticsVO getStatistics(AdminHomeQueryParam param);
}
