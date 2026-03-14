package com.fireworks.admin.controller;

import com.fireworks.common.api.Result;
import com.fireworks.model.constant.PermissionConstant;
import com.fireworks.model.dto.PmsProductAddParam;
import com.fireworks.model.dto.PmsProductQueryParam;
import com.fireworks.model.dto.PmsProductUpdateParam;
import com.fireworks.model.pojo.PmsProduct;
import com.fireworks.model.vo.PageResult;
import com.fireworks.model.vo.PmsProductListVO;
import com.fireworks.service.PmsProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理接口。
 */
@RestController
@RequestMapping("/admin")
public class PmsProductController {

    private final PmsProductService pmsProductService;

    public PmsProductController(PmsProductService pmsProductService) {
        this.pmsProductService = pmsProductService;
    }

    @GetMapping("/product/list")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).PRODUCT_LIST)")
    public Result<PageResult<PmsProductListVO>> listPage(PmsProductQueryParam param) {
        return Result.success(pmsProductService.listPage(param));
    }

    @GetMapping("/product/{id}")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).PRODUCT_EDIT)")
    public Result<PmsProduct> getById(@PathVariable Long id) {
        return Result.success(pmsProductService.getById(id));
    }

    @PostMapping("/product")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).PRODUCT_ADD)")
    public Result<PmsProduct> add(@RequestBody PmsProductAddParam param) {
        return Result.success(pmsProductService.add(param));
    }

    @PutMapping("/product/{id}")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).PRODUCT_EDIT)")
    public Result<Void> update(@PathVariable Long id, @RequestBody PmsProductUpdateParam param) {
        pmsProductService.update(id, param);
        return Result.success();
    }
}
