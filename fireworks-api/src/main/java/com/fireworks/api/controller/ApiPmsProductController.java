package com.fireworks.api.controller;

import com.fireworks.common.api.Result;
import com.fireworks.model.dto.PmsProductQueryParam;
import com.fireworks.model.vo.ApiProductDetailVO;
import com.fireworks.model.vo.PageResult;
import com.fireworks.model.vo.PmsProductListVO;
import com.fireworks.service.PmsProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端 - 商品接口（公开，无需认证）。
 */
@RestController
@RequestMapping("/api")
public class ApiPmsProductController {

    private final PmsProductService pmsProductService;

    public ApiPmsProductController(PmsProductService pmsProductService) {
        this.pmsProductService = pmsProductService;
    }

    /**
     * 分页查询商品列表，仅返回上架商品（status=1），忽略请求中的 status，避免下架/待上架商品暴露给 C 端。
     */
    @GetMapping("/product/list")
    public Result<PageResult<PmsProductListVO>> listPage(PmsProductQueryParam param) {
        param.setStatus(1);
        return Result.success(pmsProductService.listPage(param));
    }

    /**
     * 根据 ID 获取商品详情。
     */
    @GetMapping("/product/{id}")
    public Result<ApiProductDetailVO> getById(@PathVariable Long id) {
        return Result.success(pmsProductService.getProductDetail(id));
    }
}
