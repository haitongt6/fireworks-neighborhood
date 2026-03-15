package com.fireworks.api.controller;

import com.fireworks.api.service.ApiCategoryCacheService;
import com.fireworks.common.api.Result;
import com.fireworks.model.pojo.PmsProductCategory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端 - 商品分类接口（公开，无需认证），优先走缓存。
 */
@RestController
@RequestMapping("/api")
public class ApiPmsCategoryController {

    private final ApiCategoryCacheService apiCategoryCacheService;

    public ApiPmsCategoryController(ApiCategoryCacheService apiCategoryCacheService) {
        this.apiCategoryCacheService = apiCategoryCacheService;
    }

    /**
     * 获取全部商品分类列表，用于首页侧边栏等。优先查 Redis 缓存。
     */
    @GetMapping("/category/list")
    public Result<List<PmsProductCategory>> listAll() {
        return Result.success(apiCategoryCacheService.listAll());
    }
}
