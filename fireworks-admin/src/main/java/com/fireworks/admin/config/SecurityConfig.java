package com.fireworks.admin.config;

import com.fireworks.admin.security.JwtAuthenticationTokenFilter;
import com.fireworks.service.security.AdminUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置。
 *
 * <h3>配置要点</h3>
 * <ul>
 *   <li>禁用 CSRF（前后端分离 RESTful 场景）</li>
 *   <li>禁用 Session，使用 STATELESS 策略（JWT 无状态认证）</li>
 *   <li>登录接口 {@code POST /admin/login} 完全放行，无需 Token</li>
 *   <li>OPTIONS 预检请求放行（兼容 CORS）</li>
 *   <li>其余所有接口必须携带合法 JWT Token</li>
 *   <li>在 {@link UsernamePasswordAuthenticationFilter} 之前插入 {@link JwtAuthenticationTokenFilter}</li>
 *   <li>开启方法级权限控制（{@code @PreAuthorize}）</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AdminUserDetailsService adminUserDetailsService;
    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    public SecurityConfig(AdminUserDetailsService adminUserDetailsService,
                          JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter) {
        this.adminUserDetailsService = adminUserDetailsService;
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
    }

    /**
     * 密码编码器 Bean，采用 BCrypt 强哈希算法。
     * <p>
     * 此 Bean 同时被 {@link com.fireworks.service.impl.UmsAdminServiceImpl} 注入，
     * 用于登录时的密码比对。
     * </p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 将 {@link AuthenticationManager} 暴露为 Spring Bean。
     * <p>
     * 若后续需要在接口中手动调用认证（如验证码登录），可直接注入使用。
     * </p>
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 配置认证管理器使用的 UserDetailsService 和密码编码器。
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(adminUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    /**
     * 配置 HTTP 安全规则。
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（REST API 场景无需）
            .csrf().disable()

            // 禁用 Session，完全依赖 JWT
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

            // 请求授权规则
            .authorizeRequests()
                // 管理员登录接口放行
                .antMatchers(HttpMethod.POST, "/admin/login").permitAll()
                // 跨域预检请求放行
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                // 其余接口一律需要认证
                .anyRequest().authenticated()
            .and()

            // 禁用默认表单登录页（前后端分离无需）
            .formLogin().disable()

            // 禁用 HTTP Basic 认证（改用 JWT Bearer）
            .httpBasic().disable();

        // 在用户名密码过滤器之前插入 JWT 认证过滤器
        http.addFilterBefore(jwtAuthenticationTokenFilter,
                UsernamePasswordAuthenticationFilter.class);
    }
}
