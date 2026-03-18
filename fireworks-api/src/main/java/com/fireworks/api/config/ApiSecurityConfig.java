package com.fireworks.api.config;

import com.fireworks.api.security.ApiJwtAuthenticationFilter;
import com.fireworks.api.security.ApiRestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * C 端 Spring Security 配置。
 * <p>
 * 与 admin 模块的区别：
 * <ul>
 *   <li>大部分接口 permitAll（商品、分类、短信、登录）</li>
 *   <li>仅用户相关、购物车、订单等接口需要认证</li>
 *   <li>无角色/权限体系，不启用方法级权限控制</li>
 *   <li>无 AuthenticationManager（验证码登录不走 Security 认证流程）</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {

    private final ApiJwtAuthenticationFilter apiJwtAuthenticationFilter;
    private final ApiRestAuthenticationEntryPoint apiRestAuthenticationEntryPoint;

    public ApiSecurityConfig(ApiJwtAuthenticationFilter apiJwtAuthenticationFilter,
                             ApiRestAuthenticationEntryPoint apiRestAuthenticationEntryPoint) {
        this.apiJwtAuthenticationFilter = apiJwtAuthenticationFilter;
        this.apiRestAuthenticationEntryPoint = apiRestAuthenticationEntryPoint;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()

            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

            .authorizeRequests()
                // 公开接口
                .antMatchers(HttpMethod.POST, "/api/sms/sendVerifyCode").permitAll()
                .antMatchers(HttpMethod.POST, "/api/member/loginByPhone").permitAll()
                .antMatchers(HttpMethod.GET, "/api/category/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/product/**").permitAll()
                .antMatchers(HttpMethod.GET, "/file/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                // 需要认证的接口
                .antMatchers("/api/member/**").authenticated()
                .antMatchers("/api/cart/**").authenticated()
                .antMatchers("/api/order/**").authenticated()
                // 其余默认放行（未来新增接口可按需调整）
                .anyRequest().permitAll()
            .and()

            .formLogin().disable()
            .httpBasic().disable()

            .exceptionHandling()
                .authenticationEntryPoint(apiRestAuthenticationEntryPoint);

        http.addFilterBefore(apiJwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/file/**", config);
        return source;
    }
}
