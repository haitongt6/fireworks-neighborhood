package com.fireworks.service;

import com.fireworks.model.dto.PmsProductAddParam;
import com.fireworks.model.dto.PmsProductQueryParam;
import com.fireworks.model.dto.PmsProductUpdateParam;
import com.fireworks.model.pojo.PmsProduct;
import com.fireworks.model.vo.ApiProductDetailVO;
import com.fireworks.model.vo.PageResult;
import com.fireworks.model.vo.PmsProductListVO;

/**
 * 商品服务接口。
 */
public interface PmsProductService {

    /**
     * 分页查询商品列表。
     */
    PageResult<PmsProductListVO> listPage(PmsProductQueryParam param);

    /**
     * 根据ID获取商品详情。
     */
    PmsProduct getById(Long id);

    /**
     * 根据ID获取 C 端商品详情（含类目名称等展示字段）。
     */
    ApiProductDetailVO getProductDetail(Long id);

    /**
     * 新增商品。
     */
    PmsProduct add(PmsProductAddParam param);

    /**
     * 更新商品。
     */
    void update(Long id, PmsProductUpdateParam param);
}
