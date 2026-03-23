package com.fireworks.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fireworks.model.pojo.OmsOrderStockLock;
import org.apache.ibatis.annotations.Mapper;

/**
 * 锁库存 Mapper。
 */
@Mapper
public interface OmsOrderStockLockMapper extends BaseMapper<OmsOrderStockLock> {
}
