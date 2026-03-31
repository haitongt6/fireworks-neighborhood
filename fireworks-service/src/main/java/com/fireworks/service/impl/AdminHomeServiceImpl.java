package com.fireworks.service.impl;

import com.fireworks.model.constant.OrderStatusEnum;
import com.fireworks.model.dto.AdminHomeQueryParam;
import com.fireworks.model.vo.AdminHomeStatisticsVO;
import com.fireworks.service.AdminHomeService;
import com.fireworks.service.mapper.AdminHomeMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 后台首页统计服务实现。
 */
@Service
public class AdminHomeServiceImpl implements AdminHomeService {

    private final AdminHomeMapper adminHomeMapper;

    public AdminHomeServiceImpl(AdminHomeMapper adminHomeMapper) {
        this.adminHomeMapper = adminHomeMapper;
    }

    /**
     * 聚合后台首页「首页统计」所需的所有指标。
     * <p>
     * <b>时间范围</b>：由 {@code param.startTime} / {@code param.endTime}（格式 {@code yyyy-MM-dd}）控制。
     * Mapper XML 中仅当对应非 null 且非空字符串时，才拼接
     * {@code create_time &gt;= start 00:00:00} / {@code create_time &lt;= end 23:59:59}；
     * 若未传或为空，则该维度统计不限制时间（全表在其它条件约束下）。
     * </p>
     * <p>
     * <b>返回约定</b>：与前端约定一致——金额类无数据为 {@link BigDecimal#ZERO}，
     * 计数与列表无数据为 {@code 0L} 或空列表，避免 JSON 中出现 {@code null} 导致前端判空分支膨胀。
     * </p>
     *
     * @param param 查询条件；可为 {@code null}，将按「无时间条件」处理
     * @return 非 {@code null}；内部各集合字段已尽量非空
     */
    @Override
    public AdminHomeStatisticsVO getStatistics(AdminHomeQueryParam param) {
        // 避免 NPE；空 DTO 与「未传时间」在 XML 中行为一致（不拼接时间条件）
        AdminHomeQueryParam queryParam = param;
        if (queryParam == null) {
            queryParam = new AdminHomeQueryParam();
        }

        AdminHomeStatisticsVO statisticsVO = new AdminHomeStatisticsVO();

        // 已完成订单（order_status=4）在区间内的实付金额合计；无行时 Mapper 可能返回 null
        BigDecimal totalSalesAmount = adminHomeMapper.selectTotalSalesAmount(queryParam);
        statisticsVO.setTotalSalesAmount(totalSalesAmount == null ? BigDecimal.ZERO : totalSalesAmount);

        // 未删除订单按 order_status 分组计数；再映射为 waitPay/paid/canceled/closed/finished 五档
        List<Map<String, Object>> orderStatusRows = adminHomeMapper.selectOrderStatusCount(queryParam);
        statisticsVO.setOrderStatusCount(buildOrderStatusCount(orderStatusRows));

        // 会员表在区间内的 create_time 计数
        Long newMemberCount = adminHomeMapper.selectNewMemberCount(queryParam);
        statisticsVO.setNewMemberCount(newMemberCount == null ? 0L : newMemberCount);

        // 已完成订单 + 订单项汇总，按销量取 Top5；无数据时 Mapper 可能返回 null
        List<AdminHomeStatisticsVO.TopProductVO> topProducts = adminHomeMapper.selectTopProducts(queryParam);
        statisticsVO.setTopProducts(topProducts == null ? Collections.emptyList() : topProducts);

        // 已完成订单按日聚合 pay_amount；无数据时返回空列表，并对单日 amount 做空值兜底
        List<AdminHomeStatisticsVO.SalesTrendVO> salesTrend = adminHomeMapper.selectSalesTrend(queryParam);
        if (salesTrend == null) {
            statisticsVO.setSalesTrend(Collections.emptyList());
        } else {
            for (AdminHomeStatisticsVO.SalesTrendVO trendVO : salesTrend) {
                if (trendVO.getAmount() == null) {
                    trendVO.setAmount(BigDecimal.ZERO);
                }
            }
            statisticsVO.setSalesTrend(salesTrend);
        }

        // 最近 10 条订单：不按时间区间过滤，仅 deleted=0 ORDER BY create_time DESC LIMIT 10
        List<AdminHomeStatisticsVO.RecentOrderVO> recentOrders = adminHomeMapper.selectRecentOrders();
        if (recentOrders == null) {
            statisticsVO.setRecentOrders(new ArrayList<>());
        } else {
            for (AdminHomeStatisticsVO.RecentOrderVO recentOrderVO : recentOrders) {
                if (recentOrderVO.getTotalAmount() == null) {
                    recentOrderVO.setTotalAmount(BigDecimal.ZERO);
                }
                if (recentOrderVO.getPayAmount() == null) {
                    recentOrderVO.setPayAmount(BigDecimal.ZERO);
                }
            }
            statisticsVO.setRecentOrders(recentOrders);
        }

        return statisticsVO;
    }

    /**
     * 将 Mapper {@code selectOrderStatusCount} 返回的分组结果，组装为首页所需的五档数量。
     * <p>
     * SQL 侧为 {@code SELECT order_status AS orderStatus, COUNT(1) AS count ... GROUP BY order_status}，
     * MyBatis 将每行映射为 {@code Map}，键名与列别名一致。不同 JDBC/驱动下值类型可能是
     * {@link Integer}/{@link Long}/{@link java.math.BigDecimal} 等，故对 {@code orderStatus}、{@code count}
     * 做类型分支解析；无法解析或缺少键时跳过该行。
     * </p>
     * <p>
     * <b>映射规则</b>：仅处理与 {@link OrderStatusEnum} 中「待支付 / 已支付 / 已取消 / 已关闭 / 已完成」
     * 五档 code 一致的行；其它状态（若将来扩展）本方法不写入任何字段，等效于该状态在结果中不出现。
     * </p>
     * <p>
     * <b>无数据</b>：{@code rows} 为 null 或空时，五档均为 {@code 0L}，与「有分组但某状态无订单」区分：
     * 后者该状态在 SQL 结果中无行，对应字段保持默认 {@code 0L}。
     * </p>
     *
     * @param rows Mapper 返回的列表，每行含 {@code orderStatus}、{@code count}；可为 null
     * @return 非 null，五档字段均有值（至少为 0）
     */
    private AdminHomeStatisticsVO.OrderStatusCountVO buildOrderStatusCount(List<Map<String, Object>> rows) {
        AdminHomeStatisticsVO.OrderStatusCountVO orderStatusCountVO = new AdminHomeStatisticsVO.OrderStatusCountVO();
        // 先置默认值，保证前端拿到的 Long 永不为 null
        orderStatusCountVO.setWaitPay(0L);
        orderStatusCountVO.setPaid(0L);
        orderStatusCountVO.setCanceled(0L);
        orderStatusCountVO.setClosed(0L);
        orderStatusCountVO.setFinished(0L);

        if (rows == null || rows.isEmpty()) {
            return orderStatusCountVO;
        }

        for (Map<String, Object> row : rows) {
            Object statusObj = row.get("orderStatus");
            Object countObj = row.get("count");
            if (statusObj == null || countObj == null) {
                continue;
            }

            // 与 OrderStatusEnum#getCode() 对齐的整型状态值
            Integer orderStatus = null;
            if (statusObj instanceof Number) {
                orderStatus = ((Number) statusObj).intValue();
            } else {
                orderStatus = Integer.valueOf(String.valueOf(statusObj));
            }

            Long count;
            if (countObj instanceof Number) {
                count = ((Number) countObj).longValue();
            } else {
                count = Long.valueOf(String.valueOf(countObj));
            }

            if (OrderStatusEnum.WAIT_PAY.getCode().equals(orderStatus)) {
                orderStatusCountVO.setWaitPay(count);
            } else if (OrderStatusEnum.PAID.getCode().equals(orderStatus)) {
                orderStatusCountVO.setPaid(count);
            } else if (OrderStatusEnum.CANCELED.getCode().equals(orderStatus)) {
                orderStatusCountVO.setCanceled(count);
            } else if (OrderStatusEnum.CLOSED.getCode().equals(orderStatus)) {
                orderStatusCountVO.setClosed(count);
            } else if (OrderStatusEnum.FINISHED.getCode().equals(orderStatus)) {
                orderStatusCountVO.setFinished(count);
            }
        }

        return orderStatusCountVO;
    }
}
