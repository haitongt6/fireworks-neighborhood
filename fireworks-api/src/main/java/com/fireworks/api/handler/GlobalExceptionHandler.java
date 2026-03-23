package com.fireworks.api.handler;

import com.fireworks.common.api.Result;
import com.fireworks.service.exception.BizException;
import com.fireworks.service.exception.SmsLimitException;
import com.fireworks.service.exception.SmsSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * C 端 API 全局异常处理器。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.failed(e.getMessage());
    }

    @ExceptionHandler(SmsLimitException.class)
    public Result<?> handleSmsLimitException(SmsLimitException e) {
        log.warn("短信限流: code={}, message={}", e.getCode(), e.getMessage());
        return Result.failed(e.getMessage());
    }

    @ExceptionHandler(SmsSendException.class)
    public Result<?> handleSmsSendException(SmsSendException e) {
        log.warn("短信发送失败: {}", e.getMessage());
        return Result.failed(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.warn("请求参数校验失败: {}", message);
        return Result.failed(message);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.warn("请求参数绑定失败: {}", message);
        return Result.failed(message);
    }

    /**
     * 业务参数校验失败（含购物车：库存不足、限购超限、商品下架等）。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("业务校验失败: {}", e.getMessage());
        return Result.failed(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.failed("系统繁忙，请稍后重试");
    }
}
