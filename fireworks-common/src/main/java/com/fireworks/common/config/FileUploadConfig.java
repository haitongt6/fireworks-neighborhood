package com.fireworks.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 文件访问配置：将 /file/** 与 /api/file/** 映射到上传目录。
 * <p>
 * 供 admin、API 等 Web 模块复用，确保上传的图片可在各端正常访问。
 * </p>
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:D:/fireworks-neighborhood-file}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = uploadDir.replace("\\", "/");
        if (!path.endsWith("/")) {
            path += "/";
        }
        String location = "file:" + path;
        registry.addResourceHandler("/file/**").addResourceLocations(location);
        registry.addResourceHandler("/api/file/**").addResourceLocations(location);
    }
}
