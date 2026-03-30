package com.fireworks.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireworks.model.dto.PmsProductAddParam;
import com.fireworks.model.dto.PmsProductQueryParam;
import com.fireworks.model.dto.PmsProductUpdateParam;
import com.fireworks.model.pojo.PmsProduct;
import com.fireworks.model.pojo.PmsProductCategory;
import com.fireworks.model.vo.ApiProductDetailVO;
import com.fireworks.model.vo.PageResult;
import com.fireworks.model.vo.PmsProductListVO;
import com.fireworks.service.PmsCategoryService;
import com.fireworks.service.PmsProductService;
import com.fireworks.service.mapper.PmsProductMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link PmsProductService} 实现。
 */
@Service
public class PmsProductServiceImpl implements PmsProductService {

    private final PmsProductMapper productMapper;
    private final PmsCategoryService pmsCategoryService;

    public PmsProductServiceImpl(PmsProductMapper productMapper,
                                 PmsCategoryService pmsCategoryService) {
        this.productMapper = productMapper;
        this.pmsCategoryService = pmsCategoryService;
    }

    @Override
    public PageResult<PmsProductListVO> listPage(PmsProductQueryParam param) {
        if (param.getPageNum() == null || param.getPageNum() < 1) {
            param.setPageNum(1);
        }
        if (param.getPageSize() == null || param.getPageSize() < 1) {
            param.setPageSize(10);
        }
        Page<PmsProductListVO> page = new Page<>(param.getPageNum(), param.getPageSize());
        IPage<PmsProductListVO> result = productMapper.selectProductListPage(page, param);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public PmsProduct getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        PmsProduct p = productMapper.selectById(id);
        if (p == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        return p;
    }

    @Override
    public ApiProductDetailVO getProductDetail(Long id) {
        PmsProduct p = getById(id);
        ApiProductDetailVO vo = new ApiProductDetailVO();
        BeanUtils.copyProperties(p, vo);
        if (p.getCategoryId() != null) {
            PmsProductCategory cat = pmsCategoryService.getById(p.getCategoryId());
            if (cat != null) {
                vo.setCategoryName(cat.getName());
            }
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PmsProduct add(PmsProductAddParam param) {
        if (param.getTitle() == null || param.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("商品标题不能为空");
        }
        if (param.getCategoryId() == null) {
            throw new IllegalArgumentException("类目不能为空");
        }
        if (param.getPrice() == null) {
            throw new IllegalArgumentException("价格不能为空");
        }
        if (param.getStock() == null || param.getStock() < 0) {
            throw new IllegalArgumentException("库存必填且必须大于等于0");
        }
        if (param.getImages() == null || param.getImages().trim().isEmpty()) {
            throw new IllegalArgumentException("主图至少保留一张");
        }
        if (param.getDetailPics() == null || param.getDetailPics().trim().isEmpty()) {
            throw new IllegalArgumentException("详情图至少保留一张");
        }
        PmsProduct p = new PmsProduct();
        BeanUtils.copyProperties(param, p);
        p.setTitle(p.getTitle().trim());
        p.setSubTitle(p.getSubTitle() != null ? p.getSubTitle().trim() : null);
        p.setStatus(p.getStatus() != null ? p.getStatus() : 1);
        productMapper.insert(p);
        return p;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PmsProductUpdateParam param) {
        if (param.getStock() != null && param.getStock() < 0) {
            throw new IllegalArgumentException("库存必须大于等于0");
        }
        if (param.getImages() != null && param.getImages().trim().isEmpty()) {
            throw new IllegalArgumentException("主图至少保留一张");
        }
        if (param.getDetailPics() != null && param.getDetailPics().trim().isEmpty()) {
            throw new IllegalArgumentException("详情图至少保留一张");
        }
        PmsProduct p = getById(id);
        BeanUtils.copyProperties(param, p);
        p.setTitle(p.getTitle().trim());
        p.setSubTitle(p.getSubTitle() != null ? p.getSubTitle().trim() : null);
        productMapper.updateById(p);
    }
}
