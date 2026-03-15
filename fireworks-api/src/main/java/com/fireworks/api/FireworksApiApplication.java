package com.fireworks.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 烟火邻里 - C 端对外 API 启动入口（端口 8080）。
 *
 * <p>
 * 扫描 {@code fireworks-service} 中的 Service、Mapper，供 Controller 注入使用。
 * </p>
 */
@SpringBootApplication(scanBasePackages = {"com.fireworks.api", "com.fireworks.service", "com.fireworks.common"})
@MapperScan("com.fireworks.service.mapper")
public class FireworksApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FireworksApiApplication.class, args);
    }
}

