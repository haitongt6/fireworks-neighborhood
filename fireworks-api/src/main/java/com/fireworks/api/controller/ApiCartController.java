package com.fireworks.api.controller;

import com.fireworks.common.api.ApiMemberDetails;
import com.fireworks.common.api.Result;
import com.fireworks.model.dto.CartAddParam;
import com.fireworks.model.dto.CartRemoveParam;
import com.fireworks.model.dto.CartUpdateParam;
import com.fireworks.model.vo.CartItemVO;
import com.fireworks.service.OmsCartService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端购物车接口。
 * <p>所有接口需登录（Security 配置统一拦截），未登录返回 401。</p>
 */
@RestController
@RequestMapping("/api/cart")
public class ApiCartController {

    private final OmsCartService cartService;

    public ApiCartController(OmsCartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 获取购物车列表。
     */
    @GetMapping("/list")
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.list(currentUserId()));
    }

    /**
     * 加购。
     */
    @PostMapping("/add")
    public Result<Void> add(@Validated @RequestBody CartAddParam param) {
        cartService.add(currentUserId(), param);
        return Result.success();
    }

    /**
     * 修改数量。
     */
    @PostMapping("/update")
    public Result<Void> update(@Validated @RequestBody CartUpdateParam param) {
        cartService.update(currentUserId(), param);
        return Result.success();
    }

    /**
     * 删除单项。
     */
    @PostMapping("/remove")
    public Result<Void> remove(@Validated @RequestBody CartRemoveParam param) {
        cartService.remove(currentUserId(), param);
        return Result.success();
    }

    /**
     * 清空购物车。
     */
    @PostMapping("/clear")
    public Result<Void> clear() {
        cartService.clear(currentUserId());
        return Result.success();
    }

    private Long currentUserId() {
        ApiMemberDetails details = (ApiMemberDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return details.getMember().getId();
    }
}
