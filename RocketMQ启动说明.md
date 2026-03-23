# RocketMQ 4.9.8 本地启动说明（Windows）

> MQ 安装目录：`D:\codeing-tools\rocketmq-all-4.9.8-bin-release`
> 所有命令均在 **cmd（管理员）** 中执行，先启动 NameServer，再启动 Broker。

---

## 一、启动顺序

### 1. 启动 NameServer

```cmd
cd D:\codeing-tools\rocketmq-all-4.9.8-bin-release\bin
start mqnamesrv.cmd
```

启动成功标志（新窗口输出）：
```
The Name Server boot success. serializeType=JSON
```

---

### 2. 启动 Broker

```cmd
cd D:\codeing-tools\rocketmq-all-4.9.8-bin-release\bin
start mqbroker.cmd -n 127.0.0.1:9876 autoCreateTopicEnable=true
```

> `-n 127.0.0.1:9876` 指定 NameServer 地址（默认端口 9876）  
> `autoCreateTopicEnable=true` 开发环境自动创建 Topic，生产环境请关闭

启动成功标志（新窗口输出）：
```
The broker[brokerName, 127.0.0.1:10911] boot success...
```

---

## 二、停止服务

### 停止 Broker
```cmd
cd D:\codeing-tools\rocketmq-all-4.9.8-bin-release\bin
mqshutdown.cmd broker
```

### 停止 NameServer
```cmd
cd D:\codeing-tools\rocketmq-all-4.9.8-bin-release\bin
mqshutdown.cmd namesrv
```

---

## 三、常用管理命令（mqadmin）

```cmd
cd D:\codeing-tools\rocketmq-all-4.9.8-bin-release\bin

# 创建 / 更新 Topic
mqadmin.cmd updateTopic -b 127.0.0.1:10911 -t <TopicName>

# 创建 / 更新 消费者组
mqadmin.cmd updateSubGroup -b 127.0.0.1:10911 -g <GroupName>

# 查看集群状态
mqadmin.cmd clusterList -n 127.0.0.1:9876

# 查看 Topic 列表
mqadmin.cmd topicList -n 127.0.0.1:9876
```

---

## 四、本项目用到的 Topic & Group

| 用途 | Topic | ConsumerGroup |
|---|---|---|
| 购物车异步落库 | `cart-persist-topic` | `cart-persist-group` |

> 如未开启 `autoCreateTopicEnable=true`，请手动执行：
> ```cmd
> mqadmin.cmd updateTopic -b 127.0.0.1:10911 -t cart-persist-topic
> mqadmin.cmd updateSubGroup -b 127.0.0.1:10911 -g cart-persist-group
> ```

---

## 五、默认端口一览

| 服务 | 端口 |
|---|---|
| NameServer | 9876 |
| Broker 监听（Producer/Consumer） | 10911 |
| Broker HA 端口 | 10912 |
| Broker VIP 通道 | 10909 |

---

## 六、常见问题

### Q1：启动 Broker 报 `Cannot allocate memory`
Broker 默认 JVM 堆内存较大，修改 `bin\runbroker.cmd`，找到 `-Xms` / `-Xmx` 参数并调小：
```
set "JAVA_OPT=%JAVA_OPT% -server -Xms512m -Xmx512m -Xmn256m"
```

### Q2：启动 NameServer 报内存不足
修改 `bin\runserver.cmd`：
```
set "JAVA_OPT=%JAVA_OPT% -server -Xms256m -Xmx256m -Xmn128m"
```

### Q3：ROCKETMQ_HOME 未设置
在系统环境变量中新增：
```
变量名：ROCKETMQ_HOME
变量值：D:\codeing-tools\rocketmq-all-4.9.8-bin-release
```

### Q4：消息堆积 / 验证收发
使用 RocketMQ Dashboard（原 rocketmq-console）可视化监控，或用以下命令验证：
```cmd
# 发送测试消息
set NAMESRV_ADDR=127.0.0.1:9876
tools.cmd org.apache.rocketmq.example.quickstart.Producer

# 消费测试消息
tools.cmd org.apache.rocketmq.example.quickstart.Consumer
```
