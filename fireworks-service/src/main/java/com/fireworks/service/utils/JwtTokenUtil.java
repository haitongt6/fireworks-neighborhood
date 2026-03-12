package com.fireworks.service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 工具类（基于 JJWT 0.9.1）。
 * <p>
 * Token 仅存储用户名（subject）和创建时间，不持久化角色/权限信息。
 * 会话有效性由 Redis 控制（登录写入、修改密码删除），JWT 过期时间作为绝对上限。
 * </p>
 */
@Component
public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    private static final String CLAIM_KEY_CREATED = "created";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400}")
    private Long expiration;

    /**
     * 根据 {@link UserDetails} 生成 JWT Token。
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<String, Object>(2);
        claims.put(CLAIM_KEY_CREATED, new Date());
        return buildToken(claims, userDetails.getUsername());
    }

    /**
     * 从 Token 中解析用户名（即 JWT 的 subject）。
     *
     * @return 用户名；Token 无效或过期时返回 {@code null}
     */
    public String getUsernameFromToken(String token) {
        try {
            return getClaimsFromToken(token).getSubject();
        } catch (Exception e) {
            log.warn("从 Token 解析用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 校验 Token 是否合法：签名正确且 subject 与 UserDetails 的用户名一致。
     * <p>
     * 会话过期由 Redis TTL 控制，此处不再校验 JWT 自身的过期时间。
     * </p>
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return username != null && username.equals(userDetails.getUsername());
    }

    private String buildToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
