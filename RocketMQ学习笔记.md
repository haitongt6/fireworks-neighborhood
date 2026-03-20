# RocketMQ 学习笔记

> 本文档整理自项目实践与阿里云官方文档，便于后期学习与巩固。  
> 适用：Fireworks Neighborhood 项目、RocketMQ 4.x、rocketmq-spring-boot-starter 2.2.3

---

## 目录

1. [核心概念速览](#一核心概念速览)
2. [整体架构与四大角色](#二整体架构与四大角色)
3. [连接建立流程](#三连接建立流程)
4. [Topic、Group、Tag 详解](#四topicgrouptag-详解)
5. [订阅关系一致性](#五订阅关系一致性)
6. [一个 Topic 被多个 Group 订阅](#六一个-topic-被多个-group-订阅)
7. [本项目中的 RocketMQ 配置](#七本项目中的-rocketmq-配置)

---

## 一、核心概念速览

| 概念 | 一句话定义 | 本项目示例 |
|------|------------|------------|
| **Topic** | 消息主题，一类消息的集合，类似「表名」 | `cart-persist` |
| **Producer Group** | 生产者组，标识同一类 Producer 实例（同一业务、同一角色） | `cart-producer-group` |
| **Consumer Group** | 消费者组，标识同一类 Consumer 实例，组内负载均衡、组间独立消费 | `cart-consumer-group` |
| **Tag** | Topic 下的二级分类，用于区分同一 Topic 中不同业务类型 | 当前未使用，可扩展为 `UPSERT`、`DELETE`、`CLEAR` |
| **MessageQueue** | Topic 下的物理队列，消息实际写入这里，一个 Topic 通常有多个 Queue | 默认 4 个 |

---

## 二、整体架构与四大角色

### 2.1 架构图（含 Topic、Group、Queue）

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        RocketMQ 服务端（NameServer + Broker）                     │
└─────────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────────┐
│  NameServer (127.0.0.1:9876)                                                      │
│  职责：路由注册中心，维护 Topic → Broker → Queue 的映射，不存消息                    │
│                                                                                  │
│  路由表示意：                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │ Topic: cart-persist                                                          │ │
│  │   └── Broker (127.0.0.1:10911)                                               │ │
│  │         ├── Queue0  (queueId=0)                                              │ │
│  │         ├── Queue1  (queueId=1)                                              │ │
│  │         ├── Queue2  (queueId=2)                                              │ │
│  │         └── Queue3  (queueId=3)                                              │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────────────┘
                    ▲                                    ▲
                    │ ① 查询 Topic 路由                  │ ① 查询 Topic 路由
                    │    返回 Broker + Queue 列表         │    返回 Broker + Queue 列表
                    │                                    │
┌───────────────────┴──────────────────────────────────┴───────────────────────────┐
│                                                                                   │
│  Broker（消息存储服务器）                                                           │
│  职责：接收、存储、投递消息                                                          │
│                                                                                   │
│  Topic: cart-persist 的物理存储：                                                   │
│  ┌─────────────────────────────────────────────────────────────────────────────┐  │
│  │  CommitLog（所有消息顺序写）  │  ConsumeQueue（按 Queue 建索引，供消费）        │  │
│  │  msg1, msg2, msg3, ...       │  Queue0 → [offset1, offset2, ...]             │  │
│  │                              │  Queue1 → [offset3, offset4, ...]             │  │
│  └─────────────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────────────┘
                    ▲                                    │
                    │ ② 发送消息到指定 Queue               │ ③ 从 Queue 拉取消息
                    │    (hashKey=userId 选 Queue)        │
                    │                                    ▼
┌───────────────────┴───────────────────────────────────────────────────────────────┐
│                           API 应用（FireworksApiApplication）                       │
│                                                                                   │
│  ┌─────────────────────────────────────┐  ┌─────────────────────────────────────┐ │
│  │ Producer 侧                          │  │ Consumer 侧                          │ │
│  │ Producer Group: cart-producer-group  │  │ Consumer Group: cart-consumer-group   │ │
│  │ RocketMQTemplate → CartMqProducer     │  │ CartPersistConsumer                  │ │
│  │ Topic: cart-persist                  │  │ Topic: cart-persist                   │ │
│  │ hashKey: userId → 选 Queue           │  │ 订阅 Topic，按 Queue 串行消费         │ │
│  └─────────────────────────────────────┘  └─────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 四大角色说明

| 角色 | 职责 | 类比 |
|------|------|------|
| **NameServer** | 路由注册中心，维护 Topic→Broker→Queue 映射，不存消息 | 类似「DNS」或「注册中心」 |
| **Broker** | 消息存储与投递，接收 Producer 消息、服务 Consumer 拉取 | 消息的「仓库」 |
| **Producer** | 发送消息到指定 Topic | 消息的「生产者」 |
| **Consumer** | 从 Topic 拉取并消费消息 | 消息的「消费者」 |

### 2.3 启动顺序建议

1. 先启动 **NameServer**（`mqnamesrv.cmd`）
2. 再启动 **Broker**（`mqbroker.cmd -n 127.0.0.1:9876 autoCreateTopicEnable=true`）
3. 最后启动 **API 应用**

---

## 三、连接建立流程

### 3.1 自动装配机制

项目引入 `rocketmq-spring-boot-starter` 后，连接由 **Spring Boot 自动装配** 完成：

| 阶段 | 动作 |
|------|------|
| Spring 容器启动 | 加载 `RocketMQAutoConfiguration`，读取 `rocketmq.*` 配置 |
| 创建 Producer | 实例化 `DefaultMQProducer`，设置 `namesrvAddr`、`producerGroup` |
| 创建 RocketMQTemplate | 封装 Producer，注入到 `CartMqProducer` |
| 扫描 Consumer | 发现带 `@RocketMQMessageListener` 的类 |
| 创建 Consumer | 实例化 `DefaultMQPushConsumer`，设置 `namesrvAddr`、`consumerGroup`、`topic` |
| 启动 | 调用 `producer.start()`、`consumer.start()`，建立与 NameServer 的长连接 |

### 3.2 连接建立时序

```
应用启动
    │
    ├─► Producer.start()  ──► 连接 NameServer (127.0.0.1:9876)
    │
    └─► Consumer.start()  ──► 连接 NameServer (127.0.0.1:9876)

发送消息时：
    Producer ──► 向 NameServer 查询 Topic 路由 ──► 得到 Broker 地址
            ──► 连接 Broker ──► 发送消息

消费消息时：
    Consumer ──► 向 NameServer 查询 Topic 路由 ──► 得到 Broker 地址
            ──► 连接 Broker ──► 拉取消息
```

### 3.3 一次「加购」的完整数据流

```
用户点击「加购」 → OmsCartServiceImpl.addItem()
                        │
                        ▼
① Producer 发送
   CartMqProducer.sendUpsert(userId=1001, productId=5, ...)
   → sendOneWayOrderly(topic="cart-persist", hashKey="1001")
   → 向 NameServer 查路由 → 选 Queue1 (1001%4=1) → 连 Broker 发送
                        │
                        ▼
② Broker 存储
   消息写入 CommitLog，更新 Queue1 的 ConsumeQueue 索引
                        │
                        ▼
③ Consumer 消费
   CartPersistConsumer 从 Queue1 拉取 → onMessage() → handleUpsert() → 落库 MySQL
```

---

## 四、Topic、Group、Tag 详解

### 4.1 三者关系图

```
Topic（一级分类，消息通道）
  │
  ├── Tag A（二级分类，业务类型1）  ← 可选，用于过滤
  ├── Tag B（业务类型2）
  └── Tag C（业务类型3）
  │
  └── MessageQueue（物理队列，默认 4 个）
        Queue0 | Queue1 | Queue2 | Queue3

Producer Group：谁在发（同一类 Producer）
Consumer Group：谁在收（同一类 Consumer，组内负载均衡）
```

### 4.2 Topic 与 MessageQueue

```
Topic: cart-persist
         │
         ▼
┌────────────────────────────────────────────────────────────────────┐
│  Queue0        Queue1        Queue2        Queue3                   │
│  ┌──────┐     ┌──────┐     ┌──────┐     ┌──────┐                   │
│  │userId │     │userId │     │userId │     │userId │                 │
│  │%4=0   │     │%4=1   │     │%4=2   │     │%4=3   │  ← 顺序消息     │
│  │的消息 │     │的消息 │     │的消息 │     │的消息 │    hashKey=userId │
│  └──────┘     └──────┘     └──────┘     └──────┘    同一用户固定进   │
│                                                      同一 Queue      │
└────────────────────────────────────────────────────────────────────┘
```

### 4.3 Tag 的用途

| 订阅表达式 | 含义 |
|------------|------|
| `*` | 消费该 Topic 下所有消息 |
| `order-created` | 只消费 Tag=order-created |
| `order-created \|\| order-paid` | 消费 order-created 或 order-paid |
| `order-*` | 支持 Tag 模糊匹配（部分版本） |

Tag 是**消息级属性**，不同 Tag 的消息可存在同一 Queue，Consumer 通过 Tag 表达式过滤。

### 4.4 Group 的作用

| Group 类型 | 作用 |
|------------|------|
| **Producer Group** | ① 标识同一类 Producer ② 事务消息回查、故障转移时按组管理 |
| **Consumer Group** | ① 组内负载均衡（每条消息只被组内一个实例消费）② 组间独立（不同 Group 各自消费一份） |

**「同一类」的含义**：同一业务、同一角色、可互相替代的多个实例。

---

## 五、订阅关系一致性

> 参考：[阿里云 - 订阅关系一致性](https://help.aliyun.com/zh/apsaramq-for-rocketmq/cloud-message-queue-rocketmq-4-x-series/use-cases/subscription-consistency)

### 5.1 核心定义

**订阅关系一致** = 同一个 **Consumer Group** 下，所有 Consumer 实例订阅的 **Topic 和 Tag 必须完全一致**。

### 5.2 正确 vs 错误

| 场景 | 是否允许 | 说明 |
|------|----------|------|
| **一个 Topic 被多个 Group 订阅** | ✅ 允许 | 不同 Group 各自消费一份，互不影响，是常见用法 |
| **同一 Group 内，不同 Consumer 订阅不一致** | ❌ 不允许 | 属于订阅关系混乱，会导致消息分配异常、重复或遗漏 |

### 5.3 订阅关系混乱的典型错误

| 错误类型 | 示例 |
|----------|------|
| Topic 不一致 | C1 订阅 TopicA，C2 订阅 TopicB，C3 订阅 TopicC |
| Tag 不一致 | C1 订阅 Tag1，C2、C3 订阅 Tag2 |
| Tag 顺序不一致 | C1 订阅 `Tag1\|\|Tag2`，C2、C3 订阅 `Tag2\|\|Tag1` |

### 5.4 正确示例（同一 Group 内）

```
Group: cart-consumer-group
  C1、C2、C3 都订阅：topic=cart-persist, tag=*（或都不指定 tag）
  → 订阅关系一致 ✅
```

---

## 六、一个 Topic 被多个 Group 订阅

### 6.1 典型业务场景

**本质**：一条业务事件，需要被多个下游系统各自独立处理，互不依赖。

```
                    一条消息（事件）
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
         ▼                 ▼                 ▼
    Group A            Group B            Group C
    （落库）           （扣库存）          （加积分）
    各自消费一份，互不干扰
```

### 6.2 场景分类

| 场景类型 | 说明 | 示例 |
|----------|------|------|
| **事件驱动** | 一个领域事件，多个订阅方 | 订单支付成功 → 落库、扣库存、加积分、发通知 |
| **数据同步** | 主数据变更，多个系统要同步 | 商品上下架 → 搜索索引、推荐、缓存 |
| **审计/埋点** | 业务操作需要多类记录 | 下单 → 业务落库、审计日志、数据统计 |
| **解耦下游** | 上游只发事件，下游各自订阅 | 用户注册 → 发欢迎短信、初始化积分 |

### 6.3 本项目未来可参考的业务

#### 订单支付成功

```
Topic: order-events, Tag: order-paid

Group 1: order-persist-group      → 订单状态更新为已支付
Group 2: inventory-deduct-group  → 扣减商品库存
Group 3: points-calc-group       → 增加用户积分
Group 4: coupon-invalidate-group → 核销优惠券
Group 5: notification-group      → 发送支付成功短信/推送
```

#### 商品上下架

```
Topic: product-events, Tag: product-online | product-offline

Group 1: product-persist-group   → 更新商品状态
Group 2: search-index-group      → 同步到 Elasticsearch
Group 3: cache-invalidate-group  → 清理商品详情缓存
```

#### 用户注册

```
Topic: user-events, Tag: user-registered

Group 1: user-persist-group  → 用户落库
Group 2: welcome-sms-group   → 发欢迎短信
Group 3: points-init-group   → 新用户送积分
```

### 6.4 设计原则小结

| 原则 | 说明 |
|------|------|
| Topic = 一类业务事件 | 如 `order-events`、`product-events` |
| Tag = 事件子类型 | 如 `order-paid`、`order-created` |
| Group = 一个下游职责 | 一个 Group 只做一件事 |
| 一条消息多 Group | 同一事件被多个下游各自消费，实现解耦和扩展 |

---

## 七、本项目中的 RocketMQ 配置

### 7.1 配置文件

```yaml
# fireworks-api/src/main/resources/application.yml
rocketmq:
  name-server: 127.0.0.1:9876
  producer:
    group: cart-producer-group
    send-message-timeout: 3000
```

### 7.2 依赖

```groovy
// fireworks-service/build.gradle
implementation 'org.apache.rocketmq:rocketmq-spring-boot-starter:2.2.3'
```

### 7.3 生产者

- **类**：`CartMqProducer`
- **Topic**：`cart-persist`
- **Producer Group**：`cart-producer-group`（来自配置）
- **发送方式**：`sendOneWayOrderly(topic, message, hashKey=userId)`

### 7.4 消费者

- **类**：`CartPersistConsumer`
- **注解**：`@RocketMQMessageListener(topic = "cart-persist", consumerGroup = "cart-consumer-group", consumeMode = ConsumeMode.ORDERLY)`
- **消费逻辑**：`onMessage()` → `handleUpsert` / `handleDelete` / `handleClear` → 落库 MySQL

### 7.5 消息流转

```
OmsCartServiceImpl（加购/改数量/删除/清空）
    │
    ├─► 更新 Redis
    │
    └─► CartMqProducer.sendXxx()
            │
            └─► RocketMQ (topic=cart-persist, hashKey=userId)
                    │
                    └─► CartPersistConsumer.onMessage()
                            │
                            └─► 落库 MySQL (oms_cart_item)
```

---

## 附录：概念对照表

| 概念 | 本项目配置/代码位置 | 在流程中的角色 |
|------|-------------------|----------------|
| **Topic** | `CartMqProducer.TOPIC = "cart-persist"` | 消息分类，Producer 发到它，Consumer 订阅它 |
| **Producer Group** | `application.yml` 中 `rocketmq.producer.group` | 标识发送方，用于事务和故障转移 |
| **Consumer Group** | `@RocketMQMessageListener(consumerGroup = "cart-consumer-group")` | 标识消费方，组内负载均衡、组间独立 |
| **MessageQueue** | 由 Broker 创建，默认 4 个 | Topic 下的物理队列，顺序消息用 hashKey 选 Queue |
| **Tag** | 当前未使用 | Topic 下的二级分类，用于过滤 |

---

*文档最后更新：根据项目实践整理*
