package com.fireworks.api.security;

import com.fireworks.common.api.ApiMemberDetails;
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
 * C 端 JWT 认证过滤器。
 * <p>
 * 解析 Token → Redis 获取会员会话 → 校验 → 写入 SecurityContext。
 * Redis 中无数据视为会话过期，放行由后续 Security 返回 401。
 * 每次认证通过时滑动续期 Redis 会话（30 分钟）。
 * </p>
 */
@Component
public class ApiJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiJwtAuthenticationFilter.class);

    private final JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.tokenHeader:Authorization}")
    private String tokenHeader;

    @Value("${jwt.tokenHead:Bearer }")
    private String tokenHead;

    public ApiJwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(tokenHeader);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(tokenHead)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(tokenHead.length());
        String phone = jwtTokenUtil.getUsernameFromToken(token);

        if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            ApiMemberDetails memberDetails = RedisUtil.get(
                    RedisKeyConstant.API_MEMBER_INFO_KEY + phone,
                    ApiMemberDetails.class);

            if (memberDetails == null) {
                log.debug("Redis 中未找到会员 [{}] 的会话，Token 已过期或被登出", phone);
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtTokenUtil.validateToken(token, memberDetails)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                memberDetails, null, memberDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                RedisUtil.expire(RedisKeyConstant.API_MEMBER_INFO_KEY + phone,
                        30, TimeUnit.MINUTES);

                log.debug("C 端会员 [{}] JWT 认证通过", phone);
            } else {
                log.warn("C 端会员 [{}] 的 JWT Token 校验失败", phone);
            }
        }

        filterChain.doFilter(request, response);
    }
}
