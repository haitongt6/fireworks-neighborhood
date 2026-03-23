package com.fireworks.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireworks.model.dto.OmsOrderQueryParam;
import com.fireworks.model.pojo.OmsOrder;
import com.fireworks.model.vo.OmsOrderListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单主表 Mapper。
 */
@Mapper
public interface OmsOrderMapper extends BaseMapper<OmsOrder> {

    IPage<OmsOrderListItemVO> selectOrderPage(Page<OmsOrderListItemVO> page, @Param("param") OmsOrderQueryParam param);
}
