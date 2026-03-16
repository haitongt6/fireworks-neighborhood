package com.fireworks.api.handler;

import com.fireworks.common.api.Result;
import com.fireworks.service.exception.SmsLimitException;
import com.fireworks.service.exception.SmsSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * C 端 API 全局异常处理器。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return Result.failed(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.failed("系统繁忙，请稍后重试");
    }
}
