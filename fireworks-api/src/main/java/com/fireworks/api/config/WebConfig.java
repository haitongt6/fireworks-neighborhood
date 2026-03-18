package com.fireworks.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置。
 * <p>
 * CORS 已在 {@link ApiSecurityConfig} 中通过 {@code CorsConfigurationSource} 统一配置，
 * 确保 Spring Security 过滤器链在 CORS 预检阶段正确放行。
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:D:/fireworks-neighborhood-file}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = uploadDir.replace("\\", "/");
        if (!path.endsWith("/")) {
            path += "/";
        }
        registry.addResourceHandler("/file/**").addResourceLocations("file:" + path);
    }
}
