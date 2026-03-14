package com.fireworks.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireworks.model.dto.PmsProductQueryParam;
import com.fireworks.model.vo.PmsProductListVO;
import org.apache.ibatis.annotations.Param;

/**
 * 商品 Mapper。
 */
public interface PmsProductMapper extends BaseMapper<com.fireworks.model.pojo.PmsProduct> {

    /**
     * 分页查询商品列表，关联类目名称。
     */
    IPage<PmsProductListVO> selectProductListPage(Page<PmsProductListVO> page, @Param("param") PmsProductQueryParam param);
}
