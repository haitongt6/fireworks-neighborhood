package com.fireworks.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fireworks.model.dto.AdminHomeQueryParam;
import com.fireworks.model.pojo.OmsOrder;
import com.fireworks.model.vo.AdminHomeStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 后台首页统计 Mapper。
 */
@Mapper
public interface AdminHomeMapper extends BaseMapper<OmsOrder> {

    BigDecimal selectTotalSalesAmount(@Param("param") AdminHomeQueryParam param);

    List<Map<String, Object>> selectOrderStatusCount(@Param("param") AdminHomeQueryParam param);

    Long selectNewMemberCount(@Param("param") AdminHomeQueryParam param);

    List<AdminHomeStatisticsVO.TopProductVO> selectTopProducts(@Param("param") AdminHomeQueryParam param);

    List<AdminHomeStatisticsVO.SalesTrendVO> selectSalesTrend(@Param("param") AdminHomeQueryParam param);

    List<AdminHomeStatisticsVO.RecentOrderVO> selectRecentOrders();
}
