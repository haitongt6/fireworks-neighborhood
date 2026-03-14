package com.fireworks.admin.controller;

import com.fireworks.common.api.Result;
import com.fireworks.model.constant.PermissionConstant;
import com.fireworks.model.dto.PmsCategoryAddParam;
import com.fireworks.model.dto.PmsCategoryUpdateParam;
import com.fireworks.model.pojo.PmsProductCategory;
import com.fireworks.service.PmsCategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品类目管理接口。
 */
@RestController
@RequestMapping("/admin")
public class PmsCategoryController {

    private final PmsCategoryService pmsCategoryService;

    public PmsCategoryController(PmsCategoryService pmsCategoryService) {
        this.pmsCategoryService = pmsCategoryService;
    }

    @GetMapping("/category/list")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).CATEGORY_LIST)")
    public Result<List<PmsProductCategory>> listAll() {
        return Result.success(pmsCategoryService.listAll());
    }

    @PostMapping("/category")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).CATEGORY_ADD)")
    public Result<PmsProductCategory> add(@RequestBody PmsCategoryAddParam param) {
        return Result.success(pmsCategoryService.add(param));
    }

    @PutMapping("/category/{id}")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).CATEGORY_EDIT)")
    public Result<Void> update(@PathVariable Long id, @RequestBody PmsCategoryUpdateParam param) {
        pmsCategoryService.update(id, param);
        return Result.success();
    }

    @DeleteMapping("/category/{id}")
    @PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).CATEGORY_DELETE)")
    public Result<Void> delete(@PathVariable Long id) {
        pmsCategoryService.delete(id);
        return Result.success();
    }
}
