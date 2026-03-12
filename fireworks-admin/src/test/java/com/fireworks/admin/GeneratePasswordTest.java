package com.fireworks.admin;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 工具测试类：生成 BCrypt 密码哈希，供数据库初始化使用。
 * 运行后将打印出可直接写入 ums_admin.password 字段的哈希值。
 */
public class GeneratePasswordTest {

    @Test
    public void generateAndVerify() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "123456";
        String hash = encoder.encode(rawPassword);

        System.out.println("========================================");
        System.out.println("原始密码  : " + rawPassword);
        System.out.println("BCrypt哈希: " + hash);
        System.out.println("验证结果  : " + encoder.matches(rawPassword, hash));
        System.out.println("========================================");
        System.out.println("-- 用以下 SQL 更新数据库中的密码：");
        System.out.println("UPDATE ums_admin SET password = '" + hash + "' WHERE username = 'admin';");
        System.out.println("========================================");
    }
}
