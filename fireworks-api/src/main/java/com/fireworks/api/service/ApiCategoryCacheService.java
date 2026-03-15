package com.fireworks.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fireworks.model.constant.RedisKeyConstant;
import com.fireworks.model.pojo.PmsProductCategory;
import com.fireworks.service.PmsCategoryService;
import com.fireworks.service.utils.RedisUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * C 端分类列表缓存服务。
 * <p>
 * 优先从 Redis 读取，未命中再查 DB 并写入缓存。admin 模块继续直接查 DB。
 * </p>
 */
@Service
public class ApiCategoryCacheService {

    private static final TypeReference<List<PmsProductCategory>> LIST_TYPE =
            new TypeReference<List<PmsProductCategory>>() {
            };

    private static final long CACHE_TTL_MINUTES = 10;

    private final PmsCategoryService pmsCategoryService;

    public ApiCategoryCacheService(PmsCategoryService pmsCategoryService) {
        this.pmsCategoryService = pmsCategoryService;
    }

    /**
     * 获取分类列表，优先走缓存。
     */
    public List<PmsProductCategory> listAll() {
        List<PmsProductCategory> cached = RedisUtil.get(RedisKeyConstant.API_CATEGORY_LIST, LIST_TYPE);
        if (cached != null) {
            return cached;
        }
        List<PmsProductCategory> list = pmsCategoryService.listAll();
        RedisUtil.set(RedisKeyConstant.API_CATEGORY_LIST, list, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return list;
    }
}
