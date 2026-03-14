package com.fireworks.service;

import com.fireworks.model.dto.PmsCategoryAddParam;
import com.fireworks.model.dto.PmsCategoryUpdateParam;
import com.fireworks.model.pojo.PmsProductCategory;

import java.util.List;

/**
 * 商品类目服务。
 */
public interface PmsCategoryService {

    List<PmsProductCategory> listAll();

    PmsProductCategory add(PmsCategoryAddParam param);

    void update(Long id, PmsCategoryUpdateParam param);

    void delete(Long id);
}
