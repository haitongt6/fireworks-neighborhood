package com.fireworks.admin.handler;

import com.fireworks.common.api.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常拦截器。
 * <p>
 * 统一捕获 Controller 层抛出的异常，转换为 {@link Result} 响应返回。
 * Controller 不再需要 try-catch，由 Service 抛出业务异常，此处统一处理。
 * </p>
 * <p>
 * 注意：{@link AccessDeniedException} 来自 @PreAuthorize 方法级权限校验失败，
 * 在 DispatcherServlet/MVC 层抛出，由本类捕获。而 RestAccessDeniedHandler 仅处理
 * 过滤器层的 URL 级鉴权失败，两者触发场景不同。
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 登录失败：用户名不存在或密码错误。
     * <p>统一返回模糊描述，防止用户枚举攻击。</p>
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
     * <p>
     * 该方法级权限在 Controller 方法调用前由 Spring AOP 校验，异常在 MVC 层抛出，
     * 故由本类捕获，不会走 RestAccessDeniedHandler。
     * </p>
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.forbidden("权限不足，禁止访问");
    }

    /**
     * 入参校验失败（@Valid 触发的 JSR-303 校验）。
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidationException(Exception e) {
        BindingResult br = e instanceof MethodArgumentNotValidException
                ? ((MethodArgumentNotValidException) e).getBindingResult()
                : ((BindException) e).getBindingResult();
        String message = br.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("入参校验失败: {}", message);
        return Result.failed(message);
    }

    /**
     * 业务参数校验失败，如用户名已存在、角色为空等。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("业务参数校验失败: {}", e.getMessage());
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
