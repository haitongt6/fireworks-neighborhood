package com.fireworks.service.exception;

/**
 * 通用业务异常。
 * <p>
 * {@code bizCode == 0} 表示未指定业务码，全局异常处理中映射为默认失败码（如 500）。
 * </p>
 */
public class BizException extends RuntimeException {

    /** 0 表示未指定，由处理器使用默认失败码 */
    private final int bizCode;

    public BizException(String message) {
        super(message);
        this.bizCode = 0;
    }

    public BizException(int bizCode, String message) {
        super(message);
        this.bizCode = bizCode;
    }

    public int getBizCode() {
        return bizCode;
    }
}
