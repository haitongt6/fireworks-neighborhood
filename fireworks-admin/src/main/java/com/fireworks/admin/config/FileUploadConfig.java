package com.fireworks.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 文件访问配置：将 /file/** 映射到上传目录。
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:D:/firrword-neighborhood-file}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = uploadDir.replace("\\", "/");
        if (!path.endsWith("/")) {
            path += "/";
        }
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:" + path);
    }
}
