package com.fireworks.service.config;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云号码认证服务（dypnsapi）SDK 配置类。
 * <p>
 * 使用 SendSmsVerifyCode 接口发送验证码，参照阿里云官方 SDK 示例。
 * 仅当 aliyun.sms.access-key-id 配置且非空时创建 Client Bean。
 * </p>
 */
@Configuration
public class AliyunSmsConfig {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsConfig.class);

    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${aliyun.sms.access-key-id:}')")
    public Client aliyunSmsClient(AliyunSmsProperties properties) throws Exception {
        Config config = new Config()
                .setAccessKeyId(properties.getAccessKeyId())
                .setAccessKeySecret(properties.getAccessKeySecret());
        config.endpoint = properties.getEndpoint() != null ? properties.getEndpoint() : "dypnsapi.aliyuncs.com";
        log.info("阿里云号码认证 Client 已初始化");
        return new Client(config);
    }
}
