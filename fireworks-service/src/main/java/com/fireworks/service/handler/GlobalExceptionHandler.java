package com.fireworks.service.handler;

import com.fireworks.common.api.Result;
import com.fireworks.service.exception.SmsLimitException;
import com.fireworks.service.exception.SmsSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器（API 与 Admin 公用）。
 * <p>
 * 统一捕获 Controller 层抛出的异常，转换为 {@link Result} 响应返回。
 * 包含：登录/权限、短信、业务参数、未知异常等。
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 登录失败：用户名不存在或密码错误。
     */
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public Result<?> handleLoginCredentialError(Exception e) {
        log.warn("登录失败 - 用户名或密码错误: {}", e.getMessage());
        return Result.failed("用户名或密码错误");
    }

    /**
     * 登录失败：账号已禁用。
     */
    @ExceptionHandler(DisabledException.class)
    public Result<?> handleDisabledException(DisabledException e) {
        log.warn("登录失败 - 账号已被禁用: {}", e.getMessage());
        return Result.failed(e.getMessage());
    }

    /**
     * 方法级权限不足（@PreAuthorize 校验失败）。
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.forbidden("权限不足，禁止访问");
    }

    /**
     * 短信限流。
     */
    @ExceptionHandler(SmsLimitException.class)
    public Result<?> handleSmsLimitException(SmsLimitException e) {
        log.warn("短信限流: code={}, message={}", e.getCode(), e.getMessage());
        return Result.failed(e.getMessage());
    }

    /**
     * 短信发送失败。
     */
    @ExceptionHandler(SmsSendException.class)
    public Result<?> handleSmsSendException(SmsSendException e) {
        log.warn("短信发送失败: {}", e.getMessage());
        return Result.failed(e.getMessage());
    }

    /**
     * 业务参数校验失败。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return Result.failed(e.getMessage());
    }

    /**
     * 兜底：未知异常统一返回通用错误提示。
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.failed("系统繁忙，请稍后重试");
    }
}
