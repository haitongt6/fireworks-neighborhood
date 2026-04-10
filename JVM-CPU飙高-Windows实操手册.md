# Windows 下 CPU 飙高排查实操手册（聚焦 `cpu-burn` 接口）

> 适用对象：`http://localhost:8080/api/internal/jvm-practice/cpu-burn?ms=120000`
>
> 目标：**只分析 CPU 飙高**，不混入 Heap / Metaspace 场景。
>
> 结论导向：通过两次 `Thread.print` + 一次 `jstat -gcutil`，快速确认 CPU 高是否由业务线程空转导致。

---

## 1. 本文档解决什么问题

当你调用：

```text
http://localhost:8080/api/internal/jvm-practice/cpu-burn?ms=120000
```

如果发现：

- Apifox 一直转圈
- 本机 Java 进程 CPU 升高
- 想知道是不是接口本身导致
- 想从 `thread-print-cpu-A.txt` / `thread-print-cpu-B.txt` 快速定位到关键代码

就按本文档操作。

---

## 2. 先理解这个接口为什么会打高 CPU

接口源码的核心逻辑如下：

```44:55:fireworks-api/src/main/java/com/fireworks/api/controller/ApiJvmPracticeController.java
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
```

关键点：

- 请求线程内执行 `while` 忙等循环
- 循环中没有 `sleep`、没有 IO 阻塞、没有锁等待
- 所以请求持续期间，该线程会一直占用 CPU

这意味着：

> 只要你在接口执行期间抓线程栈，理论上就应该能抓到一个 `http-nio-8080-exec-*` 线程处于 `RUNNABLE`，并停在 `ApiJvmPracticeController.cpuBurn(...)`。

---

## 3. 演练前提

### 3.1 启动参数建议

只做 CPU 排查时，建议不要刻意压小 Metaspace，避免混入其他故障：

```text
-Xmx256m -Xms256m
```

不建议本章节继续带着：

```text
-XX:MaxMetaspaceSize=64m
```

原因：它更适合 Metaspace 演练，不适合纯 CPU 排查。

### 3.2 准备产物目录

```powershell
mkdir d:\code\fireworks-neighborhood\.jvm-practice -ErrorAction SilentlyContinue
```

---

## 4. 标准排查步骤

## 4.1 锁定 Java 进程 PID

```powershell
jps -lvm
```

找到：

```text
com.fireworks.api.FireworksApiApplication
```

记下 PID，例如：

```powershell
$PID_JAVA = 8132
```

再确认 8080 对应的是不是同一个进程：

```powershell
netstat -ano | findstr :8080
```

如果最后一列 PID 和 `$PID_JAVA` 一致，说明你排查的是正确进程。

---

## 4.2 先用一个窗口发起 CPU 压力

在 **窗口 A** 里执行：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/internal/jvm-practice/cpu-burn?ms=120000"
```

说明：

- 这个命令会持续约 120 秒才返回
- 在这 120 秒内，请不要关闭窗口 A
- 这是为了保证请求线程一直在跑，便于窗口 B 抓现场

---

## 4.3 在另一个窗口抓两次线程栈

在 **窗口 B** 里执行：

```powershell
jcmd $PID_JAVA Thread.print > d:\code\fireworks-neighborhood\.jvm-practice\thread-print-cpu-A.txt
timeout /t 5
jcmd $PID_JAVA Thread.print > d:\code\fireworks-neighborhood\.jvm-practice\thread-print-cpu-B.txt
```

为什么要抓两次：

- 单次抓到某个线程，只能说明“它此刻在跑”
- 连续两次都抓到同一个线程、同一个栈位置，才能证明“它持续在跑”

---

## 4.4 再补一个 GC 视角

继续在窗口 B 执行：

```powershell
jstat -gcutil $PID_JAVA 1000 5
```

如果想保存到文件，建议这样执行：

```powershell
jstat -gcutil $PID_JAVA 1000 5 > d:\code\fireworks-neighborhood\.jvm-practice\jstat-gcutil-cpu.txt
```

作用：

- 看 CPU 高时，是否伴随频繁 GC
- 如果 GC 很平稳，而线程栈明确卡在业务方法，则更能说明是业务线程空转，而不是 GC 抢 CPU

---

## 5. 生成文件后，怎么快速定位关键代码

这是最重要的实战部分。

你现在通常会拿到这些文件：

- `thread-print-cpu-A.txt`
- `thread-print-cpu-B.txt`
- `jstat-gcutil-cpu.txt`

注意：**不要从头到尾硬读全文**。正确方法是“带目标搜索”。

---

## 6. 快速定位线程热点的最短路径

## 6.1 第一步：先搜 `RUNNABLE`

打开 `thread-print-cpu-A.txt`，直接全文搜索：

```text
java.lang.Thread.State: RUNNABLE
```

为什么先搜这个：

- CPU 高优先怀疑正在运行的线程
- `WAITING` / `TIMED_WAITING` 大多不是热点

### 你会看到三类常见线程

#### 第一类：业务请求线程

例如：

```text
"http-nio-8080-exec-5"
```

这是重点关注对象。

#### 第二类：Tomcat 底层网络线程

例如：

```text
"http-nio-8080-Acceptor"
"http-nio-8080-Poller"
```

它们经常也是 `RUNNABLE`，但通常只是网络监听或轮询，不是这次业务热点的主因。

#### 第三类：中间件线程

例如：

```text
NettyClientWorkerThread_*
redisson-*
lettuce-*
```

这些要结合调用栈判断，不能只看线程名。

---

## 6.2 第二步：优先看 `http-nio-8080-exec-*`

在 `thread-print-cpu-A.txt` 中继续搜索：

```text
http-nio-8080-exec-
```

你要重点看的是：

- 哪个 `exec-*` 线程是 `RUNNABLE`
- 它下面的前几行调用栈是什么

如果你看到类似：

```text
"http-nio-8080-exec-5" #72 daemon ...
   java.lang.Thread.State: RUNNABLE
   at com.fireworks.api.controller.ApiJvmPracticeController.cpuBurn(ApiJvmPracticeController.java:56)
```

那么这已经是**强证据**了。

解释：

- `http-nio-8080-exec-5`：Tomcat 请求处理线程
- `RUNNABLE`：线程此刻正在执行，不是在等待
- `ApiJvmPracticeController.cpuBurn(...)`：正在执行你刚调用的接口方法

---

## 6.3 第三步：为什么不能只看线程名，还要看调用栈

因为有些线程虽然也是 `RUNNABLE`，但并不一定是问题根因。

比如这种：

```text
"http-nio-8080-Poller"
   java.lang.Thread.State: RUNNABLE
   at sun.nio.ch.WindowsSelectorImpl$SubSelector.poll0(Native Method)
```

这是 Tomcat 的底层 IO 轮询线程，属于正常存在。

再比如这种：

```text
"http-nio-8080-exec-9"
   java.lang.Thread.State: WAITING (parking)
   at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
```

这说明线程在等任务，不会打高 CPU。

所以一定要看两件事：

1. 线程状态是不是 `RUNNABLE`
2. 栈顶是不是你的业务代码

---

## 6.4 第四步：在第二份文件中验证同一个线程

打开 `thread-print-cpu-B.txt`，重复同样的搜索：

- 搜 `http-nio-8080-exec-5`
- 或搜 `ApiJvmPracticeController.cpuBurn`

如果在 B 文件中，你又看到了：

```text
"http-nio-8080-exec-5"
   java.lang.Thread.State: RUNNABLE
   at com.fireworks.api.controller.ApiJvmPracticeController.cpuBurn(ApiJvmPracticeController.java:56)
```

这就说明：

- 间隔 5 秒后
- 还是这个线程
- 还是这个方法
- 还是 `RUNNABLE`

于是可以得出结论：

> 这个线程不是一瞬间经过，而是持续占用 CPU。

---

## 7. 如何把线程栈定位回源码

拿到这一行：

```text
at com.fireworks.api.controller.ApiJvmPracticeController.cpuBurn(ApiJvmPracticeController.java:56)
```

你就可以直接回项目里定位：

- 文件：`fireworks-api/src/main/java/com/fireworks/api/controller/ApiJvmPracticeController.java`
- 方法：`cpuBurn`
- 行号：`56` 附近

关键代码就是这里：

```44:55:fireworks-api/src/main/java/com/fireworks/api/controller/ApiJvmPracticeController.java
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
```

你最终要确认的就是：

- 栈里出现的方法是否和你请求的接口一致
- 方法内部是否确实存在明显 CPU 密集逻辑

在本案例里，答案都是“是”。

---

## 8. 一眼区分“有问题”和“没问题”的线程

### 8.1 可以优先怀疑的线程特征

满足下面 3 条，基本就是重点嫌疑：

1. 线程名是 `http-nio-8080-exec-*`
2. 状态是 `RUNNABLE`
3. 栈顶在项目业务代码里，例如 controller / service

### 8.2 通常不是 CPU 热点的线程特征

如果你看到下面这些，大多可以先放过：

#### 等队列

```text
LinkedBlockingQueue.take
ArrayBlockingQueue.take
```

#### 休眠

```text
Thread.sleep
```

#### 等锁/等对象

```text
Unsafe.park
Object.wait
```

#### Tomcat 底层监听线程

```text
http-nio-8080-Acceptor
http-nio-8080-Poller
```

注意：它们不是完全不重要，而是**不是本次 `cpu-burn` CPU 飙高的主因**。

---

## 9. `jstat -gcutil` 怎么辅助判断

如果你把输出保存到了：

```text
.jvm-practice\jstat-gcutil-cpu.txt
```

你主要看：

- `YGC` 是否在短时间内疯狂增长
- `FGC` 是否增长
- `GCT` 是否异常大

### 场景 A：GC 很平稳

如果表现是：

- `YGC` 小幅增长或几乎不变
- `FGC` 不变
- `GCT` 很低

结合 thread dump 命中 `cpuBurn()`，就可以判断：

> CPU 高主要来自业务线程忙等，不是 GC 造成的。

### 场景 B：GC 很频繁

如果表现是：

- `YGC` 快速增长
- 甚至 `FGC` 也增长

则说明：

> CPU 高可能不只是 `cpuBurn()`，还可能混入了频繁 GC。

但就 `cpu-burn` 这个案例来说，只要 thread dump 连续命中 `cpuBurn()`，它至少已经是明确热点之一。

---

## 10. 推荐的最短排查命令清单

### 10.1 发起 CPU 压力

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/internal/jvm-practice/cpu-burn?ms=120000"
```

### 10.2 抓线程栈

```powershell
jcmd $PID_JAVA Thread.print > d:\code\fireworks-neighborhood\.jvm-practice\thread-print-cpu-A.txt
timeout /t 5
jcmd $PID_JAVA Thread.print > d:\code\fireworks-neighborhood\.jvm-practice\thread-print-cpu-B.txt
```

### 10.3 抓 GC 数据

```powershell
jstat -gcutil $PID_JAVA 1000 5 > d:\code\fireworks-neighborhood\.jvm-practice\jstat-gcutil-cpu.txt
```

### 10.4 快速搜索关键字

打开文件后优先搜：

```text
java.lang.Thread.State: RUNNABLE
http-nio-8080-exec-
ApiJvmPracticeController.cpuBurn
LinkedBlockingQueue.take
Unsafe.park
```

---

## 11. 本案例的标准分析结论模板

你可以直接复用下面这段总结：

```text
调用 /api/internal/jvm-practice/cpu-burn?ms=120000 后，应用 CPU 升高。
通过两次 jcmd Thread.print 采样，发现同一业务线程 http-nio-8080-exec-5 持续处于 RUNNABLE，且调用栈稳定停留在 com.fireworks.api.controller.ApiJvmPracticeController.cpuBurn(ApiJvmPracticeController.java:56)。
结合源码可知，该方法在请求线程内执行 while 忙等循环，不包含 sleep、IO 阻塞或锁等待，因此可判定本次 CPU 飙高由 cpu-burn 演练接口主动制造，属于预期现象，而非框架或中间件异常。
```

---

## 12. 如果想更快，记住这 4 句话就够了

1. **先搜 `RUNNABLE`**
2. **再找 `http-nio-8080-exec-*`**
3. **看栈顶是不是项目业务代码**
4. **对比 A/B 两次是否还是同一个线程同一个方法**

满足这四步，基本就能快速锁定 CPU 热点。

---

## 13. Windows 线上 JVM 排查最简命令卡片

目标：

- 不依赖 Arthas
- 只用 JDK 自带工具
- 优先解决线上最常见的 CPU / GC / 线程排查问题

适用前提：

- 机器上安装的是 **JDK**，不是只有 JRE
- 当前用户对目标 Java 进程有访问权限
- 优先在问题发生时现场执行，不要等问题消失后再抓

---

### 13.1 先查 Java 进程 PID

什么时候用：

- 不知道当前机器上哪个 PID 是目标应用
- 准备执行 `jcmd`、`jstat` 前

命令：

```powershell
jps -lvm
```

看什么结果：

- 找到你的主类，例如：

```text
com.fireworks.api.FireworksApiApplication
```

- 记下前面的 PID，例如：

```powershell
$PID_JAVA = 8132
```

说明：

- `jps` 用来找“查谁”
- 后续所有 JVM 诊断命令都要依赖这个 PID

---

### 13.2 CPU 高时抓线程快照

什么时候用：

- 线上 CPU 飙高
- 想知道到底哪个线程在忙、在跑哪段代码

命令：

```powershell
jcmd $PID_JAVA Thread.print > D:\temp\thread.txt
```

看什么结果：

打开 `thread.txt` 后优先搜索：

```text
java.lang.Thread.State: RUNNABLE
http-nio-8080-exec-
```

重点判断：

- 是否存在 `RUNNABLE` 的业务线程
- 栈顶是否停在项目代码，如 controller / service
- 如果是 Web 场景，优先看 `http-nio-8080-exec-*`

说明：

- 这是“找 CPU 热点线程”的最核心命令
- 最好连续抓两次，中间间隔 3~5 秒，便于判断是否持续占用 CPU

补充示例：

```powershell
jcmd $PID_JAVA Thread.print > D:\temp\thread-A.txt
timeout /t 5
jcmd $PID_JAVA Thread.print > D:\temp\thread-B.txt
```

---

### 13.3 看 GC 是否异常参与

什么时候用：

- CPU 高时，想排除是不是 GC 抢 CPU
- 怀疑内存抖动、频繁 Young GC / Full GC

命令：

```powershell
jstat -gcutil $PID_JAVA 1000 5
```

如果想保存到文件：

```powershell
jstat -gcutil $PID_JAVA 1000 5 > D:\temp\jstat-gcutil.txt
```

看什么结果：

重点看这几列：

- `YGC`：Young GC 次数
- `FGC`：Full GC 次数
- `GCT`：GC 总耗时

快速判断：

- 如果 `YGC/FGC` 基本不变：说明 GC 不明显，CPU 更可能来自业务线程
- 如果 `YGC` 快速增长，甚至 `FGC` 增长：说明 GC 也在明显参与

说明：

- `jstat` 看的是“JVM 统计指标”
- 它不能告诉你具体哪段业务代码忙，但能告诉你 GC 是否异常

---

### 13.4 堆问题时手工导出 dump

什么时候用：

- 怀疑堆内存泄漏
- 想留现场给 MAT 分析
- 想看大对象、引用链、Dominator Tree

命令：

```powershell
jcmd $PID_JAVA GC.heap_dump D:\temp\heap.hprof
```

看什么结果：

- 命令成功后会在指定路径生成 `heap.hprof`
- 后续用 Eclipse MAT 打开分析

说明：

- 这是“堆现场快照”
- 对排查内存泄漏非常有用
- 但不适合直接分析 CPU 热点

---

### 13.5 看 JVM 支持哪些诊断命令

什么时候用：

- 不确定 `jcmd` 还能做什么
- 想知道当前 JVM 支持哪些排查指令

命令：

```powershell
jcmd $PID_JAVA help
```

看什么结果：

- 会列出当前 JVM 支持的命令，如：
  - `Thread.print`
  - `GC.heap_dump`
  - `VM.native_memory`
  - `VM.flags`
  - `GC.class_histogram`

说明：

- 这个命令适合临场快速探索
- 不会直接解决问题，但能告诉你下一步还能查什么

---

### 13.6 最小排查路径（推荐背下来）

如果你在线上只想做最简单、最实用的 JVM 排查，记住这 3 步：

#### CPU 高

```powershell
jps -lvm
jcmd <PID> Thread.print > D:\temp\thread.txt
jstat -gcutil <PID> 1000 5
```

#### 内存高 / 怀疑泄漏

```powershell
jps -lvm
jcmd <PID> GC.heap_dump D:\temp\heap.hprof
```

---

### 13.8 Windows 线上 CPU 飙高时 3 分钟应急操作顺序

只保留最短流程，适合线上直接照着执行。

#### 第 1 步：先找目标 Java 进程

```powershell
jps -lvm
```

记下业务应用 PID，例如：

```powershell
$PID_JAVA = 8132
```

#### 第 2 步：立刻抓两次线程快照

```powershell
jcmd $PID_JAVA Thread.print > D:\temp\thread-A.txt
timeout /t 3
jcmd $PID_JAVA Thread.print > D:\temp\thread-B.txt
```

#### 第 3 步：补一份 GC 数据

```powershell
jstat -gcutil $PID_JAVA 1000 5 > D:\temp\jstat-gcutil.txt
```

#### 第 4 步：打开 `thread-A.txt` / `thread-B.txt`，只搜这 3 个关键字

```text
java.lang.Thread.State: RUNNABLE
http-nio-8080-exec-
com.fireworks
```

#### 第 5 步：30 秒内做出初判

- 如果同一个 `http-nio-8080-exec-*` 线程在 A/B 两份文件里都 `RUNNABLE`，且栈顶停在业务代码：**优先判定为业务线程热点**
- 如果 `jstat -gcutil` 里 `YGC/FGC` 快速增长：**说明 GC 也在参与**
- 如果看不懂全部线程：**先只盯 `RUNNABLE + 业务线程 + 两次重复出现` 这条线索**

#### 第 6 步：先留证据，再决定是否进一步处理

至少保留这 3 个文件：

- `D:\temp\thread-A.txt`
- `D:\temp\thread-B.txt`
- `D:\temp\jstat-gcutil.txt`

有了这三份现场文件，后续无论是你自己分析，还是交给别人协助排查，都够用了。

---

### 13.9 一句话记忆


- `jps`：找哪个 Java 进程要查
- `jcmd Thread.print`：看哪个线程在跑、跑到哪段代码
- `jstat -gcutil`：看 GC 指标是否异常
- `jcmd GC.heap_dump`：导出堆现场给 MAT 分析

---

## 14. 本文档最终结论

针对：

```text
localhost:8080/api/internal/jvm-practice/cpu-burn?ms=120000
```

本项目中 CPU 飙高的根因是：

> 请求线程进入 `ApiJvmPracticeController.cpuBurn()` 后执行持续忙等循环，导致线程长时间 `RUNNABLE`，从而占用 CPU。

这不是系统偶发异常，而是演练接口的设计目的。
