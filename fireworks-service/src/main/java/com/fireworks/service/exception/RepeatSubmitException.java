package com.fireworks.service.exception;

/**
 * 重复提交异常。
 */
public class RepeatSubmitException extends BizException {

    public RepeatSubmitException(String message) {
        super(message);
    }
}
