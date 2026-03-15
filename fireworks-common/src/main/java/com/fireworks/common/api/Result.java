package com.fireworks.common.api;

import lombok.Data;

/**
 * 统一 API 响应结果封装。
 * <p>
 * 所有 Controller 层接口均返回此类型，便于前端统一处理。
 * 使用静态工厂方法构建，构造器设为私有，避免外部直接 new。
 * </p>
 *
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> {

    /** HTTP 业务状态码 */
    private Integer code;

    /** 响应描述信息 */
    private String message;

    /** 响应载荷数据 */
    private T data;

    private Result() {
    }

    // ──────────────────────────────────────────────
    // 静态工厂方法
    // ──────────────────────────────────────────────

    /**
     * 操作成功并携带响应数据。
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应（code=200）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<T>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 操作成功，无响应数据。
     *
     * @param <T> 数据类型
     * @return 成功响应（code=200）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 操作失败，携带错误描述。
     *
     * @param message 错误描述
     * @param <T>     数据类型
     * @return 失败响应（code=500）
     */
    public static <T> Result<T> failed(String message) {
        Result<T> result = new Result<T>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 未认证（Token 不存在或已失效）。
     *
     * @param message 错误描述
     * @param <T>     数据类型
     * @return 未认证响应（code=401）
     */
    public static <T> Result<T> unauthorized(String message) {
        Result<T> result = new Result<T>();
        result.setCode(401);
        result.setMessage(message);
        return result;
    }

    /**
     * 无操作权限。
     *
     * @param message 错误描述
     * @param <T>     数据类型
     * @return 无权限响应（code=403）
     */
    public static <T> Result<T> forbidden(String message) {
        Result<T> result = new Result<T>();
        result.setCode(403);
        result.setMessage(message);
        return result;
    }
}
