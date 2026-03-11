## Fireworks Neighborhood 多模块项目搭建与学习笔记

> 适用环境：Gradle 6.8、JDK 1.8、Spring Boot 2.7.x  
> 模块：`fireworks-common`、`fireworks-model`、`fireworks-service`、`fireworks-admin`、`fireworks-api`

本笔记用于帮助你**复习与理解**当前多模块 Spring Boot 项目的整体设计与落地过程，包括：

- 模块职责与依赖关系设计
- Gradle Groovy DSL 多模块配置（含阿里云仓库、JDK 1.8、UTF‑8）
- 各模块 `build.gradle` 配置说明
- `admin` / `api` 的最小可运行 `SpringBootApplication` 启动类与端口配置
- 在 IntelliJ IDEA 的 Services 窗口中，同时运行多个 Spring Boot 应用的配置步骤
- 常见问题与排错要点

---

## 一、模块设计：职责与依赖关系

整个项目根目录名为 `fireworks-neighborhood`，通过 Gradle 多模块结构组织代码。

### 1.1 顶层模块关系

`settings.gradle` 中包含：

```groovy
rootProject.name = 'fireworks-neighborhood'

include 'fireworks-common'
include 'fireworks-model'
include 'fireworks-admin'
include 'fireworks-api'
include 'fireworks-service'
```

这意味着项目中有 5 个子模块：

- `fireworks-common`
- `fireworks-model`
- `fireworks-service`
- `fireworks-admin`
- `fireworks-api`

下面是推荐的**职责定位**与**依赖关系**。

### 1.2 `fireworks-common`：通用工具与基础能力

- **定位**：与具体业务无关的**通用工具模块**。
- **典型内容**：
  - 通用工具类（日期、字符串、集合、IO、加解密等）
  - 通用异常定义（如 `BizException`、错误码枚举）
  - 通用返回体封装（如 `Result<T>` / `ApiResponse<T>`）
  - 通用配置基类、拦截器基类（尽量不耦合具体业务）
- **依赖关系**：
  - 理想情况下尽量不依赖其他业务模块。
  - 可以被 `fireworks-service`、`fireworks-admin`、`fireworks-api` 等复用。

一句话：**整个系统的“工具箱”与“基础设施层”**。

### 1.3 `fireworks-model`：领域模型与数据结构

- **定位**：统一的**数据模型/领域模型模块**。
- **典型内容**：
  - 实体类（如 `User`, `Order`, `Product` 等）
  - DTO / VO（接口请求/响应对象、分页对象、查询条件对象）
  - 与领域相关的枚举（状态、类型、角色等）
- **设计原则**：
  - 只负责**数据结构的定义**和简单约束（如 JSR‑303 校验注解）。
  - 尽量不直接依赖具体的 Web 框架（如 `spring-web`）。

一句话：**系统的“数据语言”和“数据词典”**。

### 1.4 `fireworks-service`：业务核心服务

- **定位**：整个系统的**业务核心层**，负责业务规则和流程。
- **依赖**（已在 `build.gradle` 中体现）：
  - `implementation project(':fireworks-common')`
  - `implementation project(':fireworks-model')`
  - `implementation 'org.springframework.boot:spring-boot-starter'`
- **典型内容**：
  - 业务 Service：`UserService`, `OrderService`, `AuthService` 等
  - 业务规则实现、状态流转、聚合逻辑
  - Repository / DAO 接口（结合 JPA/MyBatis 等）
  - 与业务强相关的 Spring 配置（如事务管理、领域事件监听等）

一句话：**业务“大脑”全部集中在 `fireworks-service` 中**，上下游都依赖它而非重复实现逻辑。

### 1.5 `fireworks-admin`：后台管理 Web 应用

- **定位**：面向运营/管理员的**后台管理系统**。
- **依赖**：
  - `implementation project(':fireworks-service')`
  - `implementation 'org.springframework.boot:spring-boot-starter-web'`
- **典型内容**：
  - 后台管理端 Controller（用户管理、权限配置、数据报表等）
  - 后台认证与权限（登录、角色/菜单权限）
  - 与内部管理 UI（Vue/React 管理后台）对接的接口

一句话：**给内部人员使用的“控制台/运营后台”**，对业务的所有操作最终走 `fireworks-service`。

### 1.6 `fireworks-api`：对外 API Web 应用

- **定位**：面向前端/第三方/客户端的**统一 API 服务**。
- **依赖**：
  - `implementation project(':fireworks-service')`
  - `implementation 'org.springframework.boot:spring-boot-starter-web'`
- **典型内容**：
  - 对外 REST API Controller（如用户注册、登录、下单、查询订单等）
  - 鉴权、限流、签名校验、API 版本管理等
  - 与 Web 前端 / 移动端 / 第三方系统对接的接口

一句话：**对外暴露的“系统大门”**，但不在这里写复杂业务逻辑，而是委托给 `fireworks-service`。

---

## 二、Gradle 多模块配置（Groovy DSL，适配 Gradle 6.8）

### 2.1 根 `settings.gradle`

路径：`settings.gradle`  
作用：定义根项目名与子模块列表。

```groovy
rootProject.name = 'fireworks-neighborhood'

include 'fireworks-common'
include 'fireworks-model'
include 'fireworks-admin'
include 'fireworks-api'
include 'fireworks-service'
```

### 2.2 根 `build.gradle`：统一编译选项与仓库

路径：`build.gradle`  
主要职责：

- 声明 Spring Boot 插件与依赖管理插件（`apply false`，以便在需要的模块中显式 `apply plugin`）
- 在所有模块统一配置：
  - group / version
  - 仓库：阿里云 Maven 镜像 + `mavenCentral()` 兜底
  - JDK 1.8 编译选项
  - UTF‑8 编码
  - JUnit 5 测试
  - Spring Boot BOM（`spring-boot-dependencies:2.7.18`）

完整内容：

```groovy
plugins {
    id 'org.springframework.boot' version '2.7.18' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}

allprojects {
    group = 'com.fireworks'
    version = '0.0.1-SNAPSHOT'

    repositories {
        // 阿里云公共仓库
        maven {
            url 'https://maven.aliyun.com/repository/public'
        }
        // 阿里云 Spring 仓库
        maven {
            url 'https://maven.aliyun.com/repository/spring'
        }
        // 兜底使用 Maven Central
        mavenCentral()
    }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    tasks.withType(JavaCompile).configureEach {
        options.encoding = 'UTF-8'
    }

    tasks.withType(Javadoc).configureEach {
        options.encoding = 'UTF-8'
    }

    tasks.withType(Test).configureEach {
        useJUnitPlatform()
    }

    dependencyManagement {
        imports {
            mavenBom "org.springframework.boot:spring-boot-dependencies:2.7.18"
        }
    }

    dependencies {
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
    }
}
```

> **注意（Gradle 6.8 关键点）**  
> 不要在 `plugins {}` 中写 `id 'java' apply false`，因为 `java` 是 Gradle 核心插件，这样写会导致：  
> `Plugin 'java' is a core Gradle plugin, which is already on the classpath...`  
> 这里采用的是在 `subprojects {}` 中 `apply plugin: 'java'` 的方式。

### 2.3 各子模块 `build.gradle`

#### 2.3.1 `fireworks-common/build.gradle`

```groovy
dependencies {
    // 在此添加公共工具类等所需依赖
    // 示例：implementation 'org.springframework:spring-context'
}
```

你可以根据需要逐步增加工具相关依赖。

#### 2.3.2 `fireworks-model/build.gradle`

```groovy
dependencies {
    // 在此添加实体/DTO 等模型类相关依赖
    // 示例：implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
}
```

建议仅引入对领域模型确有必要的依赖（如 JPA 注解、校验注解等），避免耦合过多上层框架。

#### 2.3.3 `fireworks-service/build.gradle`

```groovy
dependencies {
    implementation project(':fireworks-common')
    implementation project(':fireworks-model')

    // 业务服务通常需要 Spring 基础能力
    implementation 'org.springframework.boot:spring-boot-starter'
}
```

这里通过 `project(':fireworks-common')` 和 `project(':fireworks-model')` 建立了**模块间依赖**。

#### 2.3.4 `fireworks-admin/build.gradle`

```groovy
apply plugin: 'org.springframework.boot'

dependencies {
    implementation project(':fireworks-service')

    implementation 'org.springframework.boot:spring-boot-starter-web'
}

bootJar {
    enabled = true
}

jar {
    enabled = false
}
```

说明：

- 使用 `apply plugin: 'org.springframework.boot'` 来打开 Spring Boot 能力。
- `bootJar` 启用，`jar` 禁用，表示该模块**主要产出可执行的 Boot Jar**。
- 依赖 `fireworks-service`，复用业务逻辑。

#### 2.3.5 `fireworks-api/build.gradle`

```groovy
apply plugin: 'org.springframework.boot'

dependencies {
    implementation project(':fireworks-service')

    implementation 'org.springframework.boot:spring-boot-starter-web'
}

bootJar {
    enabled = true
}

jar {
    enabled = false
}
```

与 `fireworks-admin` 的结构类似，只是提供的接口面向外部调用方。

---

## 三、最小可运行的 Spring Boot 启动类与端口配置

### 3.1 `fireworks-admin` 启动类与配置

#### 3.1.1 启动类

路径：`fireworks-admin/src/main/java/com/fireworks/admin/FireworksAdminApplication.java`

```java
package com.fireworks.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FireworksAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(FireworksAdminApplication.class, args);
    }
}
```

#### 3.1.2 端口配置

路径：`fireworks-admin/src/main/resources/application.properties`

```properties
server.port=8081
```

#### 3.1.3 启动命令（Gradle）

在项目根目录执行：

```bash
gradle :fireworks-admin:bootRun
```

### 3.2 `fireworks-api` 启动类与配置

#### 3.2.1 启动类

路径：`fireworks-api/src/main/java/com/fireworks/api/FireworksApiApplication.java`

```java
package com.fireworks.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FireworksApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FireworksApiApplication.class, args);
    }
}
```

#### 3.2.2 端口配置

路径：`fireworks-api/src/main/resources/application.properties`

```properties
server.port=8080
```

#### 3.2.3 启动命令（Gradle）

```bash
gradle :fireworks-api:bootRun
```

> 建议：端口错开（8080 / 8081），方便同时启动两个应用。

---

## 四、在 IntelliJ IDEA 中配置并同时运行多个 Spring Boot 应用

下面以 IntelliJ IDEA Ultimate 为例，说明如何在 **Services 窗口** 中同时管理 `fireworks-admin` 和 `fireworks-api` 两个应用。

### 4.1 创建 `fireworks-admin` Spring Boot 运行配置

1. 确保 IDEA 已以 Gradle 项目方式导入 `fireworks-neighborhood`。
2. 顶部菜单选择 **Run → Edit Configurations…**。
3. 左上角点击 **+**，选择 **Spring Boot**。
4. 在右侧配置：
   - **Name**：`fireworks-admin`
   - **Module**：选择 `fireworks-admin`
   - **Main class**：选择 `com.fireworks.admin.FireworksAdminApplication`
5. 勾选：
   - **Allow parallel run**（允许并行运行多个配置）
   - **Store as project file**（将配置保存在项目文件中，方便共享/备份）
6. 找到与 **Services** 相关的选项（有的版本在“Modify options”下拉里）：
   - 勾选 **Show in Services tool window** 或类似选项。

### 4.2 创建 `fireworks-api` Spring Boot 运行配置

重复以上步骤，再创建一个运行配置：

- **Name**：`fireworks-api`
- **Module**：`fireworks-api`
- **Main class**：`com.fireworks.api.FireworksApiApplication`
- 同样勾选：
  - **Allow parallel run**
  - **Store as project file**
  - **Show in Services tool window**

### 4.3 在 Services 窗口中查看与控制服务

1. 依次运行两个配置：
   - 选择运行配置 `fireworks-admin`，点击绿色 Run 按钮；
   - 再选择 `fireworks-api`，点击 Run。
2. 打开 **Services** 窗口：
   - 菜单：**View → Tool Windows → Services**。
3. 在 Services 中你可以：
   - 看到当前正在运行的两个 Spring Boot 应用；
   - 分别查看日志输出；
   - 单独停止/重启任一应用。

### 4.4 （可选）创建 Compound 组合配置，一键同时启动

1. 再次打开 **Run → Edit Configurations…**。
2. 点击 **+**，选择 **Compound**（组合配置）。
3. 在右侧配置中：
   - 给组合起名，例如 `all-services`；
   - 在 **Configurations** 列表里添加：
     - `fireworks-admin`
     - `fireworks-api`
4. 以后在运行配置下拉框中选择 `all-services`，点击 Run，即可**一次性启动两个服务**，并在 Services 窗口中统一管理。

---

## 五、常见问题与排错要点

### 5.1 Gradle 6.8 报错：Plugin 'java' is a core Gradle plugin...

错误信息类似：

> Plugin 'java' is a core Gradle plugin, which is already on the classpath. Requesting it with the 'apply false' option is a no-op.

**原因**：在 `plugins {}` 中写了：

```groovy
id 'java' apply false
```

**解决方案**：

- 删除这行；
- 在 `subprojects {}` 中使用：

```groovy
apply plugin: 'java'
```

这种写法适配 Gradle 6.8。

### 5.2 端口占用（8080 / 8081）导致 Spring Boot 启动失败

在 Windows 环境下，可以按以下步骤检查并释放端口：

1. 检查端口是否被占用（以 8080 为例）：

   ```bash
   netstat -aon | findstr :8080
   ```

   如果输出类似：

   ```text
   TCP    0.0.0.0:8080   0.0.0.0:0   LISTENING   15760
   ```

   最后一列 `15760` 即为占用该端口的进程 PID。

2. 结束对应进程：

   ```bash
   taskkill /F /PID 15760
   ```

3. 再次执行 `netstat -aon | findstr :8080`，若无输出，说明端口已释放。

4. 对 8081 等其他端口同理处理。

---

## 六、从零复盘搭建流程（作为练习 Checklist）

当你想**重新练习搭建**一个类似的多模块项目时，可以按下面的顺序走一遍：

1. **模块规划**  
   - 工具与基础：`xxx-common`  
   - 领域模型：`xxx-model`  
   - 业务核心：`xxx-service`  
   - 后台管理：`xxx-admin`  
   - 对外 API：`xxx-api`

2. **配置 `settings.gradle`**  
   - 设置 `rootProject.name`  
   - 用 `include` 声明所有子模块。

3. **编写根 `build.gradle`**  
   - 声明 Spring Boot 与依赖管理插件（`apply false`）  
   - 在 `allprojects` 中配置阿里云仓库 + `mavenCentral()`  
   - 在 `subprojects` 中统一：
     - `apply plugin: 'java'`  
     - JDK 1.8、UTF‑8 编码  
     - JUnit 5 测试  
     - Spring Boot BOM。

4. **为各子模块编写 `build.gradle`**  
   - `common`：放工具依赖  
   - `model`：放模型相关依赖  
   - `service`：依赖 `common` + `model` + `spring-boot-starter`  
   - `admin` / `api`：  
     - `apply plugin: 'org.springframework.boot'`  
     - 依赖 `service`  
     - 使用 `spring-boot-starter-web`  
     - `bootJar` 启用，`jar` 关闭。

5. **添加最小可运行的 Spring Boot 启动类与端口**  
   - 为每个可执行模块添加 `@SpringBootApplication` 启动类  
   - 在 `application.properties` 中设置不同端口。

6. **在 IDEA 中配置 Spring Boot 运行配置与 Services**  
   - 为每个可执行模块创建 Spring Boot 配置  
   - 勾选 `Allow parallel run` 与 `Show in Services tool window`  
   - 如有需要，创建 Compound 组合配置一键拉起所有服务。

7. **启动与验证**  
   - `gradle :fireworks-admin:bootRun`  
   - `gradle :fireworks-api:bootRun`  
   - 访问对应端口、排查端口占用与 Gradle 报错。

通过多次按上述步骤手动搭建，你就能真正掌握：

- Spring Boot 多模块项目的**分层设计思路**
- Gradle Groovy DSL 在多模块场景下的**通用写法**
- 在 IDE 中**高效管理多个服务实例**的实践方法。

