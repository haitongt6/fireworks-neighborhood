package com.fireworks.service.impl;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对比 {@link Runnable} 与「声明 {@code throws Exception}」的自定义函数式接口：
 * <ul>
 *   <li>{@link Runnable#run()} 不声明受检异常，Lambda 内若直接调用会抛 {@link IOException} 的方法，<strong>无法编译</strong>，
 *       必须在 Lambda 内部 try/catch 或包装为运行时异常。</li>
 *   <li>自定义接口 {@code void run() throws Exception} 时，Lambda 内可直接调用，受检异常由外层统一 catch。</li>
 * </ul>
 * <p>
 * 与 {@code OmsCartServiceImpl#executeRedisCartWriteWithRetry} 选用 {@link Runnable} 或自定义接口的取舍相关。
 * </p>
 */
class RunnableVsThrowingFunctionalInterfaceTest {

    /**
     * 与 {@link Runnable} / 自定义 {@code throws Exception} 回调对比用，仅用于抛受检异常。
     */
    private static void throwsIOException() throws IOException {
        throw new IOException("模拟受检异常");
    }

    /**
     * 若写 {@code Runnable r = () -> throwsIOException();}，当前行<strong>编译失败</strong>（未处理受检异常）。
     * 能编过的一种写法：在 Lambda 内部捕获后再抛运行时异常。
     */
    @Test
    void runnable_checked_exception_must_be_handled_inside_lambda() {
        Runnable task = () -> {
            try {
                throwsIOException();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        RuntimeException ex = assertThrows(RuntimeException.class, task::run);
        assertTrue(ex.getCause() instanceof IOException);
    }

    /**
     * 自定义函数式接口允许 {@code run()} 声明 {@code throws Exception}，Lambda 可直接写出会抛受检异常的调用。
     */
    @FunctionalInterface
    private interface MayThrowException {
        void run() throws Exception;
    }

    @Test
    void custom_functional_interface_propagates_checked_exception_to_caller() {
        MayThrowException task = () -> throwsIOException();

        assertThrows(IOException.class, task::run);
    }

    /**
     * 模拟 {@code executeRedisCartWriteWithRetry} 外层 {@code catch (Exception e)} 的接法。
     */
    @Test
    void custom_functional_interface_outer_catch_exception_unified() {
        MayThrowException task = () -> throwsIOException();

        try {
            task.run();
        } catch (Exception e) {
            assertInstanceOf(IOException.class, e);
            return;
        }
        throw new AssertionError("应进入 catch");
    }
}
