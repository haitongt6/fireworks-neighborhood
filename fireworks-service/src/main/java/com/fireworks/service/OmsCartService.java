package com.fireworks.service;

import com.fireworks.model.dto.CartAddParam;
import com.fireworks.model.dto.CartRemoveParam;
import com.fireworks.model.dto.CartUpdateParam;
import com.fireworks.model.vo.CartItemVO;

import java.util.List;

/**
 * 购物车业务服务接口。
 */
public interface OmsCartService {

    /**
     * 获取当前用户购物车列表（Redis 优先，miss 则查 MySQL 并预热）。
     */
    List<CartItemVO> list(Long userId);

    /**
     * 加购：同一商品累加数量并刷新快照，先写 Redis 再异步落库。
     */
    void add(Long userId, CartAddParam param);

    /**
     * 修改数量：先写 Redis 再异步落库。
     */
    void update(Long userId, CartUpdateParam param);

    /**
     * 删除单项：先删 Redis 再异步落库。
     */
    void remove(Long userId, CartRemoveParam param);

    /**
     * 清空购物车：先删 Redis 再异步落库。
     */
    void clear(Long userId);
}
