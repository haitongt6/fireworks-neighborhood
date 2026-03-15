package com.fireworks.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 提供 service 层依赖的 Security 相关 Bean（如 {@link PasswordEncoder}）。
 * C 端 API 当前无鉴权，仅满足 UmsAdminServiceImpl 等组件的构造器注入。
 */
@Configuration
public class SecurityBeanConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
