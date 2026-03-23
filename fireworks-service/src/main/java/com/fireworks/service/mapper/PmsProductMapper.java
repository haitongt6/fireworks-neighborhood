package com.fireworks.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireworks.model.dto.PmsProductQueryParam;
import com.fireworks.model.pojo.PmsProduct;
import com.fireworks.model.vo.PmsProductListVO;
import org.apache.ibatis.annotations.Param;

/**
 * 商品 Mapper。
 */
public interface PmsProductMapper extends BaseMapper<PmsProduct> {

    /**
     * 分页查询商品列表，关联类目名称。
     */
    IPage<PmsProductListVO> selectProductListPage(Page<PmsProductListVO> page, @Param("param") PmsProductQueryParam param);

    /**
     * 锁库存。
     */
    int lockStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 支付成功后确认扣减库存。
     */
    int confirmStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 取消/关单时释放锁库存。
     */
    int releaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
