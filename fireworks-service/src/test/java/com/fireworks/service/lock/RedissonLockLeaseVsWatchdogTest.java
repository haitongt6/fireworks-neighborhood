package com.fireworks.service.lock;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 对比 Redisson 分布式锁两种用法（与 {@code OmsOrderServiceImpl#submit} 场景相关）：
 * <ol>
 *   <li>{@code tryLock(wait, lease, unit)}：固定租约，到期 Redis 上锁消失，其它实例可再抢锁；业务若超过租约，两实例可能同时进入临界区。</li>
 *   <li>{@code tryLock(wait, unit)}：不指定 lease，走看门狗续期；持锁期间其它实例阻塞等待，临界区内并发度应为 1。</li>
 * </ol>
 * <p>
 * 需本机 Redis 可连；不可连时跳过全部测试。
 * </p>
 * <p>
 * 可通过 {@code -Dredis.test.host=} {@code -Dredis.test.port=} {@code -Dredis.test.password=} 覆盖连接
 * （与 {@code application.yml} 中密码一致时示例：{@code -Dredis.test.password=你的密码}）。
 * </p>
 * <p>
 * 「幂等」在此特指<strong>分布式锁互斥</strong>：看门狗场景下两实例不会同时进入临界区；下单完整幂等还依赖 submitToken 等业务逻辑。
 * </p>
 */
class RedissonLockLeaseVsWatchdogTest {

    private static final String LOCK_KEY_LEASE = "fireworks:test:lock:lease-overlap";
    private static final String LOCK_KEY_WATCHDOG = "fireworks:test:lock:watchdog-serial";
    private static final int REDIS_DATABASE = 15;

    private static boolean redisUp;

    @BeforeAll
    static void checkRedis() {
        redisUp = false;
        try {
            RedissonClient probe = createClient("probe");
            try {
                probe.getKeys().count();
                redisUp = true;
            } finally {
                probe.shutdown();
            }
        } catch (Exception ignored) {
            // redisUp stays false
        }
        assumeTrue(redisUp, "跳过：未检测到可用 Redis（默认 redis://127.0.0.1:6379），本类为集成测试");
    }

    /**
     * 功能点 1：显式租期小于业务耗时，两个 Redisson 客户端（模拟两台实例）可能同时进入临界区（共享资源并发修改风险）。
     */
    @Test
    void fixedLease_businessExceedsLease_twoClientsMayOverlapCriticalSection() throws Exception {
        RedissonClient clientA = createClient("A");
        RedissonClient clientB = createClient("B");
        RLock lockA = clientA.getLock(LOCK_KEY_LEASE);
        RLock lockB = clientB.getLock(LOCK_KEY_LEASE);

        AtomicInteger inCritical = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        CountDownLatch t1HoldLock = new CountDownLatch(1);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> fa = pool.submit(() -> {
                boolean ok = lockA.tryLock(5, 2, TimeUnit.SECONDS);
                if (!ok) {
                    throw new AssertionError("线程 A 应在限时内抢到锁");
                }
                t1HoldLock.countDown();
                try {
                    int n = inCritical.incrementAndGet();
                    maxConcurrent.updateAndGet(m -> Math.max(m, n));
                    // 远大于租约 2s，Redis 上 key 过期后 B 可再抢锁
                    Thread.sleep(6000L);
                } finally {
                    if (lockA.isHeldByCurrentThread()) {
                        lockA.unlock();
                    }
                }
                return null;
            });

            Future<?> fb = pool.submit(() -> {
                if (!t1HoldLock.await(10, TimeUnit.SECONDS)) {
                    throw new AssertionError("线程 A 未在预期内持锁");
                }
                Thread.sleep(500L);
                boolean ok = lockB.tryLock(15, 2, TimeUnit.SECONDS);
                if (!ok) {
                    throw new AssertionError("线程 B 应在 A 租约过期后抢到锁");
                }
                try {
                    int n = inCritical.incrementAndGet();
                    maxConcurrent.updateAndGet(m -> Math.max(m, n));
                    Thread.sleep(500L);
                } finally {
                    if (lockB.isHeldByCurrentThread()) {
                        lockB.unlock();
                    }
                }
                return null;
            });

            fa.get(40, TimeUnit.SECONDS);
            fb.get(40, TimeUnit.SECONDS);
        } finally {
            pool.shutdown();
            clientA.shutdown();
            clientB.shutdown();
        }

        assertTrue(maxConcurrent.get() >= 2,
                "固定租约且业务超过租期时，应观测到临界区内并发度>=2（maxConcurrent=" + maxConcurrent.get() + "）");
    }

    /**
     * 功能点 2：看门狗模式（不指定 lease）下，两客户端串行进入临界区，最大并发度为 1（互斥直至 unlock）。
     */
    @Test
    void watchdog_noLease_twoClientsMutuallyExclusive() throws Exception {
        RedissonClient clientA = createClient("A");
        RedissonClient clientB = createClient("B");
        RLock lockA = clientA.getLock(LOCK_KEY_WATCHDOG);
        RLock lockB = clientB.getLock(LOCK_KEY_WATCHDOG);

        AtomicInteger inCritical = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        CountDownLatch t1HoldLock = new CountDownLatch(1);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> fa = pool.submit(() -> {
                boolean ok = lockA.tryLock(5, TimeUnit.SECONDS);
                if (!ok) {
                    throw new AssertionError("线程 A 应抢到锁");
                }
                t1HoldLock.countDown();
                try {
                    int n = inCritical.incrementAndGet();
                    maxConcurrent.updateAndGet(m -> Math.max(m, n));
                    assertEquals(1, inCritical.get());
                    Thread.sleep(5000L);
                } finally {
                    inCritical.decrementAndGet();
                    if (lockA.isHeldByCurrentThread()) {
                        lockA.unlock();
                    }
                }
                return null;
            });

            Future<?> fb = pool.submit(() -> {
                if (!t1HoldLock.await(10, TimeUnit.SECONDS)) {
                    throw new AssertionError("线程 A 未在预期内持锁");
                }
                Thread.sleep(300L);
                boolean ok = lockB.tryLock(30, TimeUnit.SECONDS);
                if (!ok) {
                    throw new AssertionError("线程 B 应在 A 释放后抢到锁");
                }
                try {
                    int n = inCritical.incrementAndGet();
                    maxConcurrent.updateAndGet(m -> Math.max(m, n));
                    assertEquals(1, inCritical.get());
                    Thread.sleep(200L);
                } finally {
                    inCritical.decrementAndGet();
                    if (lockB.isHeldByCurrentThread()) {
                        lockB.unlock();
                    }
                }
                return null;
            });
            fa.get(60, TimeUnit.SECONDS);
            fb.get(60, TimeUnit.SECONDS);
        } finally {
            pool.shutdown();
            clientA.shutdown();
            clientB.shutdown();
        }

        assertEquals(1, maxConcurrent.get(),
                "看门狗续期下临界区最大并发度应为 1（maxConcurrent=" + maxConcurrent.get() + "）");
    }

    private static RedissonClient createClient(String id) {
        String host = System.getProperty("redis.test.host", "127.0.0.1");
        int port = Integer.getInteger("redis.test.port", 6379);
        String pass = System.getProperty("redis.test.password", "tht8664");
        Config config = new Config();
        config.setLockWatchdogTimeout(30_000L);
        String address = "redis://" + host + ":" + port;
        if (pass != null && !pass.isEmpty()) {
            config.useSingleServer()
                    .setAddress(address)
                    .setPassword(pass)
                    .setDatabase(REDIS_DATABASE)
                    .setClientName("test-" + id);
        } else {
            config.useSingleServer()
                    .setAddress(address)
                    .setDatabase(REDIS_DATABASE)
                    .setClientName("test-" + id);
        }
        return Redisson.create(config);
    }
}
