package com.fireworks.api.controller;

import com.fireworks.common.api.Result;
import com.fireworks.model.vo.JvmPracticeHeapStatusVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JVM 故障演练接口（任意 profile 下均注册，无需登录）。
 * <p>
 * 路径前缀 {@code /api/internal/jvm-practice}，在 {@link com.fireworks.api.config.ApiSecurityConfig} 中为 {@code permitAll}，
 * 仅用于本地排查练习；生产环境请通过网关或防火墙屏蔽。
 * </p>
 */
@RestController
@RequestMapping("/api/internal/jvm-practice")
@Api(tags = "C端-JVM演练")
public class ApiJvmPracticeController {

    private static final long CPU_BURN_MS_MAX = 120_000L;

    private static final int HEAP_LEAK_KB_MAX = 8 * 1024;

    private static final int METASPACE_LEAK_N_MAX = 100_000;

    /**
     * 模拟「按会员 ID 缓存购物车快照却永不清理」：静态 Map 持有指定 memberId 追加的 byte[]。
     */
    private static final ConcurrentHashMap<Long, List<byte[]>> HEAP_LEAK_BY_MEMBER =
            new ConcurrentHashMap<>();

    @GetMapping("/cpu-burn")
    @ApiOperation(value = "CPU飙高演练", notes = "在请求线程内空转指定毫秒；仅本地使用。")
    public Result<String> cpuBurn(
            @ApiParam(value = "空转毫秒数", required = true, example = "30000")
            @RequestParam long ms) {
        if (ms <= 0 || ms > CPU_BURN_MS_MAX) {
            return Result.failed("ms 需在 1～" + CPU_BURN_MS_MAX + " 之间");
        }
        long end = System.nanoTime() + ms * 1_000_000L;
        long sum = 0L;
        while (System.nanoTime() < end) {
            sum += System.nanoTime() & 1L;
        }
        return Result.success("cpu-burn done, junk=" + sum);
    }

    @PostMapping("/heap-leak/leak")
    @ApiOperation(value = "堆泄漏演练-追加泄漏块", notes = "向指定 memberId 追加 kb KB 的 byte[]，模拟用户维度缓存泄漏。")
    public Result<String> heapLeakLeak(
            @ApiParam(value = "会员ID（模拟用户维度）", required = true, example = "1")
            @RequestParam long memberId,
            @ApiParam(value = "追加大小（KB）", required = true, example = "512")
            @RequestParam int kb) {
        if (kb <= 0 || kb > HEAP_LEAK_KB_MAX) {
            return Result.failed("kb 需在 1～" + HEAP_LEAK_KB_MAX + " 之间");
        }
        for (int i = 0; i < 10; i++) {
            int bytes = kb * 1024;
            byte[] chunk = new byte[bytes];
            HEAP_LEAK_BY_MEMBER.computeIfAbsent(memberId, k -> new ArrayList<>()).add(chunk);
        }
        return Result.success("leaked +" + kb + "KB for member " + memberId);
    }

    @GetMapping("/heap-leak/status")
    @ApiOperation(value = "堆泄漏演练-状态")
    public Result<JvmPracticeHeapStatusVO> heapLeakStatus(
            @ApiParam(value = "会员ID", required = true, example = "1")
            @RequestParam long memberId) {
        List<byte[]> list = HEAP_LEAK_BY_MEMBER.get(memberId);
        JvmPracticeHeapStatusVO vo = new JvmPracticeHeapStatusVO();
        vo.setMemberId(memberId);
        if (list == null || list.isEmpty()) {
            vo.setLeakedBytesApprox(0L);
            vo.setChunkCount(0);
        } else {
            long sum = 0L;
            for (byte[] b : list) {
                sum += b.length;
            }
            vo.setLeakedBytesApprox(sum);
            vo.setChunkCount(list.size());
        }
        return Result.success(vo);
    }

    @PostMapping("/heap-leak/reset")
    @ApiOperation(value = "堆泄漏演练-按会员清理", notes = "清除指定 memberId 在静态 Map 中的泄漏数据。")
    public Result<String> heapLeakReset(
            @ApiParam(value = "会员ID", required = true, example = "1")
            @RequestParam long memberId) {
        HEAP_LEAK_BY_MEMBER.remove(memberId);
        return Result.success("cleared leak for member " + memberId);
    }

    @GetMapping("/metaspace-leak")
    @ApiOperation(value = "Metaspace演练", notes = "JDK8 使用 Nashorn 动态生成脚本类；需配合 -XX:MaxMetaspaceSize 小值。")
    public Result<String> metaspaceLeak(
            @ApiParam(value = "动态脚本函数个数", required = true, example = "5000")
            @RequestParam int n) {
        if (n <= 0 || n > METASPACE_LEAK_N_MAX) {
            return Result.failed("n 需在 1～" + METASPACE_LEAK_N_MAX + " 之间");
        }
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        if (engine == null) {
            return Result.failed("Nashorn 引擎不可用（当前 JDK 可能已移除 Nashorn）");
        }
        try {
            for (int i = 0; i < n; i++) {
                engine.eval("function jvm_leak_" + i + "() { return " + i + "; }");
            }
        } catch (ScriptException e) {
            return Result.failed("脚本执行失败: " + e.getMessage());
        }
        return Result.success("metaspace-leak eval count=" + n);
    }
}
