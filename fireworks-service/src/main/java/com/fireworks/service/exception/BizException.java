package com.fireworks.service.exception;

/**
 * 通用业务异常。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}
