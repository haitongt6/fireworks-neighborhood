package com.fireworks.admin.security;

import com.fireworks.common.api.AdminUserDetails;
import com.fireworks.model.constant.RedisKeyConstant;
import com.fireworks.service.utils.JwtTokenUtil;
import com.fireworks.service.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * JWT 认证过滤器。
 * <p>
 * 认证流程：解析 Token → 从 Redis 获取用户信息 → 校验 → 写入 SecurityContext + ThreadLocal。
 * Redis 中无数据视为会话已过期或被强制下线，直接放行由后续过滤器返回 401。
 * </p>
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    private final JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.tokenHeader:Authorization}")
    private String tokenHeader;

    @Value("${jwt.tokenHead:Bearer }")
    private String tokenHead;

    public JwtAuthenticationTokenFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(tokenHeader);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(tokenHead)) {
            log.debug("请求 [{} {}] 未携带有效 Authorization 头，跳过 JWT 认证", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String authToken = authHeader.substring(tokenHead.length());
        String username = jwtTokenUtil.getUsernameFromToken(authToken);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 从 Redis 加载用户信息（不查 DB）
            AdminUserDetails userDetails = RedisUtil.get(RedisKeyConstant.USER_INFO_KEY + username, AdminUserDetails.class);

            if (userDetails == null) {
                log.warn("Redis 中未找到用户 [{}] 的会话，Token 已过期或被强制下线", username);
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 刷新 Redis 过期时间（滑动窗口，用户持续活跃则不掉线）
                RedisUtil.expire(RedisKeyConstant.USER_INFO_KEY + username, 30, TimeUnit.MINUTES);

                log.debug("管理员 [{}] JWT 认证通过", username);
            } else {
                log.warn("管理员 [{}] 的 JWT Token 校验失败", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}
