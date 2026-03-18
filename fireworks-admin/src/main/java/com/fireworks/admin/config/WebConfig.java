package com.fireworks.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Admin Web 配置：静态文件资源映射。
 * 将本地文件目录映射到 /file/** 路径，供前端通过代理访问上传的图片和视频。
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
