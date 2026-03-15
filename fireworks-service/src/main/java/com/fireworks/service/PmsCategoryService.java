package com.fireworks.service;

import com.fireworks.model.dto.PmsCategoryAddParam;
import com.fireworks.model.dto.PmsCategoryUpdateParam;
import com.fireworks.model.pojo.PmsProductCategory;

import java.util.List;

/**
 * 商品类目服务。
 */
public interface PmsCategoryService {

    /**
     * 根据 ID 查询类目，不存在返回 null。
     */
    PmsProductCategory getById(Long id);

    List<PmsProductCategory> listAll();

    PmsProductCategory add(PmsCategoryAddParam param);

    void update(Long id, PmsCategoryUpdateParam param);

    void delete(Long id);
}
