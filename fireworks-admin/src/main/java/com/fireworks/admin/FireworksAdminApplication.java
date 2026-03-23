package com.fireworks.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 烟火邻里 - 后台管理端启动入口（端口 8081）。
 *
 * <p>
 * {@code scanBasePackages} 显式指定扫描范围，确保 {@code fireworks-service}
 * 模块中的 {@code @Service}、{@code @Component} 等 Bean 被正确注册到 Spring 容器。
 * </p>
 * <p>
 * {@code @MapperScan} 指定 MyBatis-Plus 的 Mapper 接口扫描路径。
 * </p>
 */
@SpringBootApplication(scanBasePackages = {"com.fireworks.admin", "com.fireworks.service", "com.fireworks.common"})
@MapperScan("com.fireworks.service.mapper")
@EnableScheduling
public class FireworksAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(FireworksAdminApplication.class, args);
    }
}
