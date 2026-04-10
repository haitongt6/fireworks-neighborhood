# Windows 下 HeapLeak 内存泄漏排查逐步实操手册（JDK 8 + MAT）

> 适用接口：`http://localhost:8080/api/internal/jvm-practice/heap-leak/leak?memberId=1&kb=8192`
>
> 目标：通过**接口调用 → JVM 指标观察 → Heap Dump → MAT 分析 → reset 验证**，完整掌握一次内存泄漏排查闭环。
>
> 适用前提：JDK 8、Windows、Spring Boot 项目已启动，且可访问 `ApiJvmPracticeController` 演练接口。

---

## 1. 本文档解决什么问题

你将通过本文档完成下面这件事：

- 人为制造一批不会被 GC 回收的 `byte[]`
- 观察 JVM 堆占用和 GC 指标变化
- 抓取 `heap dump`
- 用 MAT 找到具体泄漏对象
- 确认引用链来自 `ApiJvmPracticeController` 的静态字段
- 最后用 `reset` 验证问题可解除

换句话说，本文档不是只让你“看见内存高”，而是让你真正确认：

> **哪些对象泄漏了、为什么泄漏、是谁把它们一直引用住了。**

---

## 2. 先理解本次泄漏的代码原理

本次演练接口源码如下：

```56:74:fireworks-api/src/main/java/com/fireworks/api/controller/ApiJvmPracticeController.java
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
        int bytes = kb * 1024;
        byte[] chunk = new byte[bytes];
        HEAP_LEAK_BY_MEMBER.computeIfAbsent(memberId, k -> new ArrayList<>()).add(chunk);
        return Result.success("leaked +" + kb + "KB for member " + memberId);
    }
```

对应的静态字段如下：

```36:40:fireworks-api/src/main/java/com/fireworks/api/controller/ApiJvmPracticeController.java
    /**
     * 模拟「按会员 ID 缓存购物车快照却永不清理」：静态 Map 持有指定 memberId 追加的 byte[]。
     */
    private static final ConcurrentHashMap<Long, List<byte[]>> HEAP_LEAK_BY_MEMBER =
            new ConcurrentHashMap<>();
```

### 核心原理

每次调用接口时，都会做两件事：

1. 新建一个 `byte[]`
2. 把这个 `byte[]` 放进静态 `ConcurrentHashMap<Long, List<byte[]>>`

因为最外层是**静态字段**，所以这些 `byte[]` 不会随着请求结束而被 GC 回收。

本次你最终要在 MAT 中确认的引用链就是：

```text
ApiJvmPracticeController
 -> HEAP_LEAK_BY_MEMBER
 -> ConcurrentHashMap
 -> ArrayList
 -> Object[]
 -> byte[]
```

---

## 3. 推荐启动参数

只做 HeapLeak 演练时，建议使用下面这组参数：

```text
-Xms256m -Xmx256m
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=D:/code/fireworks-neighborhood/.jvm-practice/
```

### 参数说明

- `-Xms256m`：堆初始大小 256MB
- `-Xmx256m`：堆最大大小 256MB
- `-XX:+HeapDumpOnOutOfMemoryError`：发生 OOM 时自动导出 dump
- `-XX:HeapDumpPath=.../`：OOM dump 输出目录（建议写目录，不写固定文件名）

### 为什么不建议本次保留 `-XX:MaxMetaspaceSize=64m`

因为这是 HeapLeak 演练，不希望被 Metaspace OOM 干扰。

---

## 4. 产物目录准备

在 PowerShell 中执行：

```powershell
mkdir D:\code\fireworks-neighborhood\.jvm-practice -ErrorAction SilentlyContinue
```

建议本次演练用到的文件统一放这里，例如：

- `heap-before-oom.hprof`
- `jstat-gcutil-heap.txt`
- OOM 自动生成的 `java_pidxxxx.hprof`

---

## 5. 第一步：先确认服务与 PID

### 5.1 验证接口可访问

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/internal/jvm-practice/heap-leak/status?memberId=1"
```

只要能正常返回 JSON，就说明服务通着。

### 5.2 查 Java 进程 PID

```powershell
jps -lvm
```

找到：

```text
com.fireworks.api.FireworksApiApplication
```

记下 PID，例如：

```powershell
$PID_JAVA = 16940
```

---

## 6. 第二步：开始制造 HeapLeak

### 6.1 单次调用格式

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/internal/jvm-practice/heap-leak/leak?memberId=1&kb=8192" -Method POST
```

解释：

- `memberId=1`：把泄漏都挂到同一个 memberId 下
- `kb=8192`：每次追加 8192KB，也就是 8MB

### 6.2 推荐第一次先打 5 次

```powershell
1..5 | ForEach-Object {
  Invoke-RestMethod -Uri "http://localhost:8080/api/internal/jvm-practice/heap-leak/leak?memberId=1&kb=8192" -Method POST
}
```

这一步之后，理论上你已经追加了：

```text
5 * 8MB = 40MB
```

---

## 7. 第三步：先做业务层状态校验

### 7.1 查询状态

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/internal/jvm-practice/heap-leak/status?memberId=1"
```

### 7.2 你要看什么

返回结果里重点关注：

- `chunkCount`
- `leakedBytesApprox`

### 7.3 如何判断是否符合预期

例如你打了 5 次，每次 8MB，则理论上应接近：

- `chunkCount = 5`
- `leakedBytesApprox ≈ 41943040`

如果数量和你调用次数大致吻合，说明接口层面已经成功把泄漏对象挂进了静态 Map。

---

## 8. 第四步：观察 JVM 指标（jstat）

### 8.1 执行命令

```powershell
jstat -gcutil $PID_JAVA 1000 10 > D:\code\fireworks-neighborhood\.jvm-practice\jstat-gcutil-heap.txt
```

也可以直接打印到控制台：

```powershell
jstat -gcutil $PID_JAVA 1000 10
```

### 8.2 本次只关注这几列

- `E`：Eden 使用率
- `O`：Old 区使用率
- `YGC`：Young GC 次数
- `FGC`：Full GC 次数
- `GCT`：GC 总耗时

### 8.3 如何解读

#### 如果看到：

- `O` 持续升高
- `FGC` 开始增加
- Full GC 之后 `O` 仍然降不下来

说明：

> 这批对象正在进入老年代，而且 GC 无法有效回收，泄漏特征越来越明显。

#### 如果当前看到：

- `O` 已有一定占用
- 但 10 秒内变化不大
- `FGC` 没继续上涨

说明：

> 目前堆里已经积累了一批对象，但还没有恶化到马上 OOM。

### 8.4 重要提醒

`jstat` 只能说明：

- 内存压力有多大
- GC 是否频繁

它**不能单独证明“已经泄漏”**。真正定性还要靠 dump + MAT。

---

## 9. 第五步：手工抓一份 Heap Dump（不必等 OOM）

这是本次排查的关键一步。

### 9.1 抓 dump

```powershell
jcmd $PID_JAVA GC.heap_dump D:\code\fireworks-neighborhood\.jvm-practice\heap-before-oom.hprof
```

### 9.2 为什么不建议只等 OOM 自动 dump

因为手工抓 dump 有几个好处：

- JVM 还比较健康，成功率高
- 你能在“问题正在发生但还没彻底崩”的状态下分析
- 更适合学习和理解引用链

---

## 10. 第六步：用 MAT 打开 dump

### 10.1 打开文件

在 Eclipse MAT 中打开：

```text
D:\code\fireworks-neighborhood\.jvm-practice\heap-before-oom.hprof
```

### 10.2 如果弹出 `Leak Suspects Report`

可以生成，也可以先跳过。

建议：

- 可以看
- 但不要只依赖自动报告
- 最终还是要结合 Dominator Tree 和 Path To GC Roots 自己确认

---

## 11. 第七步：MAT 手动分析主线

本次最推荐的分析顺序是：

1. `Histogram`
2. `Dominator Tree`
3. `Path To GC Roots`
4. `Leak Suspects`

---

## 12. 第八步：先看 Histogram，确认大对象类型

### 12.1 进入 Histogram

在 MAT 中点击：

```text
Histogram
```

### 12.2 搜索 `byte[]`

在搜索框输入：

```text
byte[]
```

### 12.3 你要确认什么

- `byte[]` 数量较多
- `byte[]` 占用明显偏大

### 12.4 为什么先看 `byte[]`

因为接口代码本身就是：

```java
byte[] chunk = new byte[bytes];
```

所以这次堆里的大对象主角就是 `byte[]`。

---

## 13. 第九步：看 Dominator Tree，确认“是谁在保留内存”

### 13.1 打开 Dominator Tree

点击：

```text
Dominator Tree
```

### 13.2 搜索 `ApiJvmPracticeController`

输入：

```text
ApiJvmPracticeController
```

### 13.3 你理想中应看到的结构

展开后通常会接近这样：

```text
ApiJvmPracticeController
 -> ConcurrentHashMap
 -> ConcurrentHashMap$Node[]
 -> ConcurrentHashMap$Node
 -> ArrayList
 -> Object[]
 -> byte[]
 -> byte[]
 -> ...
```

### 13.4 如何解释这些对象

- `ConcurrentHashMap`：静态泄漏容器
- `ConcurrentHashMap$Node`：Map 节点
- `ArrayList`：某个 `memberId` 对应的 value
- `Object[]`：`ArrayList.elementData`
- `byte[]`：真正的大对象

### 13.5 如何判断已基本成立

如果你看到：

- 多个 `byte[8388608]`
- 它们都挂在同一个 `ArrayList` / `Object[]` 下
- 这个 `ArrayList` 又挂在 `ConcurrentHashMap` 下
- 最终回到 `ApiJvmPracticeController`

那么这次泄漏链已经非常明确。

---

## 14. 第十步：对一个 `byte[]` 做 Path To GC Roots

这一步是最终定性的关键。

### 14.1 操作步骤

1. 在 `Dominator Tree` 中选中任意一个大的 `byte[]`
2. 右键
3. 选择：

```text
Path To GC Roots
```

4. 在弹出的子菜单中，优先选择：

```text
exclude weak/soft references
```

### 14.2 为什么选这个

因为本次你只关心：

> 这个 `byte[]` 被哪条核心强引用链挂住了

排除 weak/soft references 后，结果更干净、更容易理解。

### 14.3 理想结果

你大概率会看到类似链条：

```text
byte[]
 <- Object[]
 <- ArrayList
 <- ConcurrentHashMap$Node
 <- ConcurrentHashMap
 <- static HEAP_LEAK_BY_MEMBER
 <- ApiJvmPracticeController
```

### 14.4 这条链意味着什么

它证明：

- `byte[]` 不是因为 GC 没工作才活着
- 而是因为它被一条静态强引用链一直持有
- 所以 GC 根本不能回收它

这就是“泄漏”的核心证据。

---

## 15. 第十一步：再看 Leak Suspects（自动报告）

### 15.1 它是什么

`Leak Suspects` 是 MAT 自动生成的“泄漏嫌疑报告”。

它会自动帮你：

- 找可疑大对象保留者
- 估算谁保留了最多内存
- 自动生成一条最短累积路径

### 15.2 它和手动分析的关系

- `Leak Suspects`：自动报告，适合快速锁定嫌疑人
- `Histogram / Dominator Tree / Path To GC Roots`：手动分析，适合理解和验证证据链

### 15.3 本次你应该看到的内容

在本次演练里，`Leak Suspects` 通常会直接指出：

- `ApiJvmPracticeController` 保留了约 80% 左右的堆
- 内存主要累计在 `Object[]`
- 路径会指向 `HEAP_LEAK_BY_MEMBER -> ConcurrentHashMap -> ArrayList -> Object[]`

### 15.4 正确理解方式

`Leak Suspects` 不是替代手动分析，而是：

> 帮你快速确认 MAT 自动识别出的“第一嫌疑人”是否和手动分析一致

如果两者一致，就说明本次分析非常稳。

---

## 16. 第十二步：写出本次分析结论

你可以直接复用下面这段：

```text
在 MAT 中通过 Histogram 观察到大量 byte[] 对象，占用了明显堆内存。
在 Dominator Tree 中可以看到，ApiJvmPracticeController 通过静态 ConcurrentHashMap 持有一个 ArrayList，该 ArrayList 的底层 Object[] 中挂载了多个约 8MB 的 byte[]。
进一步通过 Path To GC Roots 确认，这些 byte[] 通过 static HEAP_LEAK_BY_MEMBER -> ConcurrentHashMap -> ArrayList -> Object[] 的强引用链与 GC Root 相连，因此不会被垃圾回收。
结合接口代码可确认，本次堆占用增长是由 heap-leak/leak 接口持续向静态 Map 追加 byte[] 导致，属于预期的内存泄漏演练现象。
```

---

## 17. 第十三步：如果你还想体验更明显的堆压力

可以继续分批追加，而不是一次梭哈。

### 示例：再追加 5 次

```powershell
1..5 | ForEach-Object {
  Invoke-RestMethod -Uri "http://localhost:8080/api/internal/jvm-practice/heap-leak/leak?memberId=1&kb=8192" -Method POST
}
```

然后重复：

1. 查 `status`
2. 看 `jstat -gcutil`
3. 需要时再抓 dump

### 什么时候继续压到 OOM

只有当你的目标是：

- 体验 `Java heap space`
- 看 Full GC 到 OOM 的全过程
- 验证自动 OOM dump

才建议继续压到底。

否则，仅凭 dump + MAT 已经足够完成这次演练。

---

## 18. 第十四步：演练结束后做 reset 验证

### 18.1 执行 reset

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/internal/jvm-practice/heap-leak/reset?memberId=1" -Method POST
```

### 18.2 再查状态

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/internal/jvm-practice/heap-leak/status?memberId=1"
```

如果返回接近：

- `chunkCount = 0`
- `leakedBytesApprox = 0`

说明静态引用已经断开。

### 18.3 这一步为什么重要

它能帮助你真正理解：

> 问题不是 GC 不工作，而是对象被静态字段一直引用着

当引用链断开后，GC 才有机会回收这些对象。

---

## 19. 第十五步：建议的完整训练闭环

为了真正熟练掌握，我建议你把这次演练重复至少 3 轮，每轮重点不同。

### 第 1 轮：认识大对象

目标：

- 会打接口
- 会查 `status`
- 会在 Histogram 里找到 `byte[]`

### 第 2 轮：认识引用链

目标：

- 会看 Dominator Tree
- 会做 Path To GC Roots
- 能说清对象为什么不能回收

### 第 3 轮：做对比验证

目标：

- 泄漏前抓 dump
- reset 后再抓 dump
- 能对比“有引用”和“去引用”两种状态

只要你做到这三轮，这次 HeapLeak 排查你就真正掌握了。

---

## 20. 一句话总复盘

本次 HeapLeak 演练的本质不是“堆内存变大了”，而是：

> **接口每次调用都会新建一个大 `byte[]`，并把它放进 `ApiJvmPracticeController` 的静态 `HEAP_LEAK_BY_MEMBER` 中，导致这些对象被长期强引用，GC 无法回收，最终形成堆泄漏。**

你在排查时要形成的固定思路是：

```text
接口调用
 -> status 校验
 -> jstat 看趋势
 -> dump 留现场
 -> MAT 找大对象
 -> Dominator Tree 看持有者
 -> Path To GC Roots 看强引用链
 -> reset 验证问题解除
```

只要你以后都按这个闭环做，基本就能无差错地排查出这类内存泄漏问题。
