package com.fireworks.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fireworks.common.api.Result;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义未认证（401）处理器。
 * <p>
 * 当匿名用户访问需认证的接口时，Spring Security 会委托此类返回统一格式的 JSON 响应，
 * 替代默认的 403 空页面。
 * </p>
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(
                objectMapper.writeValueAsString(Result.unauthorized("未登录或 Token 已过期，请重新登录"))
        );
    }
}
