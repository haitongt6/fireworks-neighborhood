package com.fireworks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fireworks.model.dto.PmsCategoryAddParam;
import com.fireworks.model.dto.PmsCategoryUpdateParam;
import com.fireworks.model.pojo.PmsProductCategory;
import com.fireworks.service.PmsCategoryService;
import com.fireworks.service.mapper.PmsProductCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * {@link PmsCategoryService} 实现。
 */
@Service
public class PmsCategoryServiceImpl implements PmsCategoryService {

    private final PmsProductCategoryMapper categoryMapper;

    public PmsCategoryServiceImpl(PmsProductCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<PmsProductCategory> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<PmsProductCategory>()
                        .orderByAsc(PmsProductCategory::getSort)
                        .orderByAsc(PmsProductCategory::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PmsProductCategory add(PmsCategoryAddParam param) {
        if (param.getName() == null || param.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("类目名称不能为空");
        }
        PmsProductCategory c = new PmsProductCategory();
        c.setName(param.getName().trim());
        c.setSort(param.getSort() != null ? param.getSort() : 0);
        c.setStatus(param.getStatus() != null ? param.getStatus() : 1);
        c.setCreateTime(new Date());
        categoryMapper.insert(c);
        return c;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PmsCategoryUpdateParam param) {
        if (id == null) {
            throw new IllegalArgumentException("类目 ID 不能为空");
        }
        PmsProductCategory c = categoryMapper.selectById(id);
        if (c == null) {
            throw new IllegalArgumentException("类目不存在");
        }
        if (param.getName() != null) {
            c.setName(param.getName().trim());
        }
        if (param.getSort() != null) {
            c.setSort(param.getSort());
        }
        if (param.getStatus() != null) {
            c.setStatus(param.getStatus());
        }
        categoryMapper.updateById(c);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("类目 ID 不能为空");
        }
        PmsProductCategory c = categoryMapper.selectById(id);
        if (c == null) {
            throw new IllegalArgumentException("类目不存在");
        }
        categoryMapper.deleteById(id);
    }
}
