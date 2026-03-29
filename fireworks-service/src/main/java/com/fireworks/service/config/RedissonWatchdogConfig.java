package com.fireworks.service.config;

import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 看门狗：对未指定 leaseTime 的 {@link org.redisson.api.RLock} 在持锁期间自动续期。
 * <p>
 * 项目此前未显式配置，依赖 Redisson 默认 {@code lockWatchdogTimeout=30000}ms；
 * 此处显式写入，便于与文档、排障及多环境对齐。
 * </p>
 */
@Configuration
public class RedissonWatchdogConfig {

    /** 看门狗超时（毫秒）：续期周期约为 timeout/3，直至 unlock */
    private static final long LOCK_WATCHDOG_TIMEOUT_MS = 30_000L;

    @Bean
    public RedissonAutoConfigurationCustomizer redissonLockWatchdogCustomizer() {
        return (Config config) -> config.setLockWatchdogTimeout(LOCK_WATCHDOG_TIMEOUT_MS);
    }
}
