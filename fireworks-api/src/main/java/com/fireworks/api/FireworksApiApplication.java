package com.fireworks.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 烟火邻里 - C 端 API 启动入口（端口 8080）。
 * <p>
 * scanBasePackages 确保 fireworks-service 中的 @Service 等 Bean 被扫描。
 * MapperScan 确保 MyBatis Mapper 接口被注册为 Bean。
 * EnableAsync 开启异步支持（购物车异步落库）。
 * </p>
 */
@SpringBootApplication(scanBasePackages = {"com.fireworks.api", "com.fireworks.service"})
@MapperScan("com.fireworks.service.mapper")
@EnableAsync
@EnableScheduling
public class FireworksApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FireworksApiApplication.class, args);
    }
}
