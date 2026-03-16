package com.fireworks.service.exception;

/**
 * 短信限流异常。
 * <p>
 * code: -1 表示 60 秒内已发送（请稍后再试），-2 表示今日已达上限。
 * </p>
 */
public class SmsLimitException extends RuntimeException {

    /** -1: 60秒内已发送；-2: 今日已达上限 */
    private final int code;

    public SmsLimitException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
