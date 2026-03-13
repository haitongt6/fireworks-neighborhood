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

---

## 七、管理员登录功能（Spring Security + JWT）

> 对应模块：`fireworks-admin`（接入层） + `fireworks-service`（业务层）  
> 技术栈：Spring Security 5.7 · JJWT 0.9.1 · MyBatis-Plus 3.5.x · BCryptPasswordEncoder

### 7.1 整体认证流程

```
POST /admin/login
        │
        ▼
UmsAdminController          ← fireworks-admin（接收 JSON 请求体）
        │ 调用
        ▼
UmsAdminServiceImpl         ← fireworks-service（业务逻辑）
   ├─ AdminUserDetailsService.loadUserByUsername()  查 DB 加载用户+权限
   ├─ userDetails.isEnabled()                       校验账号状态
   ├─ BCryptPasswordEncoder.matches()               比对密码
   └─ JwtTokenUtil.generateToken()                  生成 JWT Token
        │
        ▼
返回 { "tokenHead": "Bearer ", "token": "eyJ..." }

后续请求
        │  Header: Authorization: Bearer eyJ...
        ▼
JwtAuthenticationTokenFilter（OncePerRequestFilter）
   ├─ 解析 Token → 取出用户名
   ├─ AdminUserDetailsService.loadUserByUsername()  实时加载权限
   ├─ JwtTokenUtil.validateToken()                  校验签名+过期
   └─ 写入 SecurityContextHolder                    标记为已认证
        │
        ▼
目标 Controller 正常执行
```

### 7.2 数据库表设计（RBAC 模型）

共 5 张表，路径：`doc/sql/fireworks_neighborhood.sql`

| 表名 | 说明 |
|------|------|
| `ums_admin` | 管理员账号（id / username / password / status / create_time） |
| `ums_role` | 角色（id / name / description / status） |
| `ums_permission` | 权限（id / pid / name / value / icon / type） |
| `ums_admin_role_relation` | 管理员 ↔ 角色（多对多） |
| `ums_role_permission_relation` | 角色 ↔ 权限（多对多） |

`ums_permission.value` 字段（如 `pms:product:read`）会被转换为 Spring Security 的 `SimpleGrantedAuthority`，供 `@PreAuthorize` 使用。

### 7.3 新增/更新的 build.gradle 依赖说明

#### `fireworks-model/build.gradle`
```groovy
dependencies {
    implementation 'com.baomidou:mybatis-plus-annotation:3.5.7'  // @TableName、@TableId 等注解
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

#### `fireworks-service/build.gradle`
```groovy
dependencies {
    implementation project(':fireworks-common')
    implementation project(':fireworks-model')
    implementation 'org.springframework.boot:spring-boot-starter'
    // 只引 security-core，不触发 Security Web 自动配置（由 admin 模块负责）
    implementation 'org.springframework.security:spring-security-core'
    implementation 'com.baomidou:mybatis-plus-boot-starter:3.5.7'
    runtimeOnly 'mysql:mysql-connector-java:8.0.33'
    implementation 'io.jsonwebtoken:jjwt:0.9.1'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

#### `fireworks-admin/build.gradle`
```groovy
apply plugin: 'org.springframework.boot'

dependencies {
    implementation project(':fireworks-service')
    // 下面三行是为了解决 Gradle implementation 编译隔离问题（见 7.6 节）
    implementation project(':fireworks-common')
    implementation project(':fireworks-model')
    implementation 'com.baomidou:mybatis-plus-boot-starter:3.5.7'

    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

### 7.4 关键文件说明

#### 7.4.1 `JwtTokenUtil`（`fireworks-service`）

路径：`fireworks-service/src/main/java/com/fireworks/service/utils/JwtTokenUtil.java`

**核心方法：**

| 方法 | 说明 |
|------|------|
| `generateToken(UserDetails)` | 根据用户名生成 JWT，写入 subject + created |
| `getUsernameFromToken(String)` | 从 Token 解析 subject（用户名），失败返回 null |
| `validateToken(String, UserDetails)` | 校验签名正确、未过期、用户名一致 |
| `isTokenExpired(String)` | 判断是否过期，解析失败也视为过期 |

**配置项（application.yml）：**

```yaml
jwt:
  secret: xxx          # HS512 签名密钥，生产环境必须替换
  expiration: 86400    # 有效期（秒），默认 24 小时
  tokenHeader: Authorization
  tokenHead: "Bearer "
```

> **设计原则**：Token 中只存用户名，不存角色/权限。每次请求实时从 DB 加载权限，保证权限变更立即生效。

#### 7.4.2 `AdminUserDetails`（`fireworks-service`）

路径：`fireworks-service/src/main/java/com/fireworks/service/security/AdminUserDetails.java`

实现 `UserDetails`，封装 `UmsAdmin` 实体和权限列表：

```java
// isEnabled() 对应 ums_admin.status 字段
public boolean isEnabled() {
    return umsAdmin.getStatus() != null && umsAdmin.getStatus() == 1;
}

// getAuthorities() 将 permission.value 转为 GrantedAuthority
public Collection<? extends GrantedAuthority> getAuthorities() {
    // 过滤 value 为 null 或空的权限节点（目录节点通常没有 value）
    ...
}
```

#### 7.4.3 `AdminUserDetailsService`（`fireworks-service`）

路径：`fireworks-service/src/main/java/com/fireworks/service/security/AdminUserDetailsService.java`

实现 Spring Security 的 `UserDetailsService`：

```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // 1. LambdaQueryWrapper 精确查用户名
    UmsAdmin admin = umsAdminMapper.selectOne(
        new LambdaQueryWrapper<UmsAdmin>().eq(UmsAdmin::getUsername, username));
    if (admin == null) throw new UsernameNotFoundException("用户名或密码错误");

    // 2. 四表联查加载权限
    List<UmsPermission> permissions = umsAdminMapper.selectPermissionByAdminId(admin.getId());
    return new AdminUserDetails(admin, permissions);
}
```

> **安全细节**：用户名不存在时抛出 `"用户名或密码错误"` 而非 `"用户不存在"`，防止用户枚举攻击。

#### 7.4.4 `UmsAdminMapper.xml`（`fireworks-service`）

路径：`fireworks-service/src/main/resources/mapper/UmsAdminMapper.xml`

权限多表联查 SQL（4 表关联）：

```xml
<select id="selectPermissionByAdminId" resultType="com.fireworks.model.pojo.UmsPermission">
    SELECT DISTINCT p.id, p.pid, p.name, p.value, p.icon, p.type
    FROM ums_admin_role_relation arr
             JOIN ums_role r ON arr.role_id = r.id AND r.status = 1
             JOIN ums_role_permission_relation rpr ON r.id = rpr.role_id
             JOIN ums_permission p ON rpr.permission_id = p.id
    WHERE arr.admin_id = #{adminId}
</select>
```

#### 7.4.5 `SecurityConfig`（`fireworks-admin`）

路径：`fireworks-admin/src/main/java/com/fireworks/admin/config/SecurityConfig.java`

继承 `WebSecurityConfigurerAdapter`，关键配置：

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()                                    // 禁用 CSRF（REST 场景）
        .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 禁用 Session
        .and()
        .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/admin/login").permitAll() // 登录接口放行
            .antMatchers(HttpMethod.OPTIONS).permitAll()              // 预检请求放行
            .anyRequest().authenticated()                             // 其余需认证
        .and()
        .formLogin().disable()
        .httpBasic().disable();

    // JWT 过滤器插在 UsernamePasswordAuthenticationFilter 之前
    http.addFilterBefore(jwtAuthenticationTokenFilter,
            UsernamePasswordAuthenticationFilter.class);
}
```

`PasswordEncoder` Bean 定义在此处，供 `UmsAdminServiceImpl` 跨模块注入：

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

#### 7.4.6 `JwtAuthenticationTokenFilter`（`fireworks-admin`）

路径：`fireworks-admin/src/main/java/com/fireworks/admin/security/JwtAuthenticationTokenFilter.java`

每次请求执行一次（继承 `OncePerRequestFilter`）：

```java
String authHeader = request.getHeader("Authorization");
if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
    String token = authHeader.substring(7);
    String username = jwtTokenUtil.getUsernameFromToken(token);
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = adminUserDetailsService.loadUserByUsername(username);
        if (jwtTokenUtil.validateToken(token, userDetails)) {
            // 构建认证对象写入 SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}
filterChain.doFilter(request, response);
```

#### 7.4.7 `FireworksAdminApplication`（`fireworks-admin`）

路径：`fireworks-admin/src/main/java/com/fireworks/admin/FireworksAdminApplication.java`

```java
@SpringBootApplication(scanBasePackages = {"com.fireworks.admin", "com.fireworks.service"})
@MapperScan("com.fireworks.service.mapper")
public class FireworksAdminApplication { ... }
```

> **要点**：  
> - `scanBasePackages` 显式指定扫描路径，确保 `fireworks-service` 中的 `@Service`、`@Component` 被注册。  
> - `@MapperScan` 指向 service 模块的 Mapper 包，MyBatis-Plus 才能找到接口。

### 7.5 配置文件：从 `.properties` 迁移到 `.yml`

**迁移原因**：`.properties` 文件在 IDEA 中中文注释需要单独配置"Native-to-ASCII"，否则显示乱码；`.yml` 默认 UTF-8，无此问题。

**完整 `application.yml`（`fireworks-admin`）：**

```yaml
server:
  port: 8081

# 数据源配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fireworks_neighborhood?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

# MyBatis-Plus 配置
mybatis-plus:
  # 扫描所有 JAR 包（含 fireworks-service 模块）classpath 下的 mapper XML 文件
  mapper-locations: classpath*:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto

# JWT 配置
jwt:
  secret: fireworks-neighborhood-jwt-secret-key-please-change-in-production
  expiration: 86400      # 单位：秒，默认 24 小时
  tokenHeader: Authorization
  tokenHead: "Bearer "   # 注意末尾保留空格
```

> **mapper-locations 说明**：`classpath*:` 会扫描所有 JAR 包的 classpath，`classpath:` 只扫描当前模块。因为 mapper XML 在 `fireworks-service` 的 JAR 里，必须用 `classpath*:`。

### 7.6 ⚠️ 重要：Gradle `implementation` 编译隔离陷阱

这是多模块项目中最容易踩的坑，本项目中连续遇到两次。

#### 原理

Gradle 的 `implementation` 配置是**编译隔离**的：

```
fireworks-service
  └── implementation project(':fireworks-common')   ← 对 service 模块编译可见
  └── implementation 'mybatis-plus-boot-starter'    ← 对 service 模块编译可见

fireworks-admin
  └── implementation project(':fireworks-service')  ← 只能看到 service 的 API，
                                                       看不到 service 的 implementation 依赖！
```

即：**`implementation` 的依赖不会传递给上层消费模块**，消费模块若直接使用这些类，编译期会报红。

与之对比，`api`（需要 `java-library` 插件）会将依赖暴露给上层。

#### 本项目踩坑记录

| 报红内容 | 根本原因 | 修复方式 |
|---------|---------|---------|
| `@MapperScan` 找不到 | `mybatis-plus-boot-starter` 在 service 用 `implementation` 引入，admin 看不到 | admin 的 `build.gradle` 补加 `implementation 'com.baomidou:mybatis-plus-boot-starter:3.5.7'` |
| `Result` 类找不到 | `fireworks-common` 在 service 用 `implementation` 引入，admin 看不到 | admin 的 `build.gradle` 补加 `implementation project(':fireworks-common')` 和 `implementation project(':fireworks-model')` |

#### 规律总结

> `fireworks-admin` 编译期**直接使用**哪个模块的类，就必须在 `fireworks-admin/build.gradle` 中**直接声明**该依赖，不能依赖 `fireworks-service` 的传递。

### 7.7 BCrypt 密码哈希生成问题及解决

#### 问题现象

调用 `POST /admin/login` 接口，账号密码正确但返回"用户名或密码错误"。

#### 根本原因

SQL 初始化脚本中 `123456` 对应的 BCrypt 哈希是**手写的占位值**，并非真实加密结果，导致 `BCryptPasswordEncoder.matches()` 返回 `false`。

#### 解决方式：用测试类生成正确哈希

创建测试类 `fireworks-admin/src/test/java/com/fireworks/admin/GeneratePasswordTest.java`：

```java
@Test
public void generateAndVerify() {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String rawPassword = "123456";
    String hash = encoder.encode(rawPassword);
    System.out.println("BCrypt哈希: " + hash);
    System.out.println("验证结果  : " + encoder.matches(rawPassword, hash));
    System.out.println("UPDATE ums_admin SET password = '" + hash + "' WHERE username = 'admin';");
}
```

运行命令：

```bash
gradle :fireworks-admin:test --tests "com.fireworks.admin.GeneratePasswordTest"
```

拿到输出的 UPDATE 语句，在数据库中执行即可。

> **重要**：每次调用 `encoder.encode()` 生成的哈希值都不同（BCrypt 内置随机 salt），但 `encoder.matches()` 均能正确验证。直接复制哈希字符串是正确的，不要自己手写。

#### 正确的初始密码哈希（`123456`，已验证）

```sql
UPDATE ums_admin SET password = '$2a$10$CtDBPecYDAXFkA8i3FVkK.s36ZeJcsIQkmK83g6FJo2kzCxrBAV62' WHERE username = 'admin';
```

### 7.8 登录接口测试

```bash
curl -X POST http://localhost:8081/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "tokenHead": "Bearer ",
    "token": "eyJhbGciOiJIUzUxMiJ9..."
  }
}
```

后续请求携带 Token：

```bash
curl http://localhost:8081/some/api \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

---

## 八、从零复盘——管理员登录功能搭建 Checklist

当你想重新练习实现 Spring Security + JWT 登录时，按以下顺序操作：

1. **数据库**：执行 `doc/sql/fireworks_neighborhood.sql`，创建 5 张表并插入测试数据
2. **实体类**（`fireworks-model`）：`UmsAdmin` / `UmsRole` / `UmsPermission`
3. **统一响应**（`fireworks-common`）：`Result<T>` 静态工厂方法
4. **Mapper**（`fireworks-service`）：`UmsAdminMapper` 继承 `BaseMapper` + XML 四表联查权限
5. **UserDetails**（`fireworks-service`）：`AdminUserDetails` 实现 `UserDetails`
6. **UserDetailsService**（`fireworks-service`）：`AdminUserDetailsService` 实现 `loadUserByUsername`
7. **JwtTokenUtil**（`fireworks-service`）：生成 / 解析 / 校验 Token
8. **Service**（`fireworks-service`）：`UmsAdminServiceImpl.login()` 串联以上组件
9. **SecurityConfig**（`fireworks-admin`）：放行登录接口，禁 Session，注册 JWT 过滤器
10. **JwtAuthenticationTokenFilter**（`fireworks-admin`）：每次请求校验 Token 并写入 SecurityContext
11. **Controller**（`fireworks-admin`）：`POST /admin/login` 返回 Token
12. **启动类**：添加 `scanBasePackages` + `@MapperScan`
13. **配置文件**：`application.yml` 补充数据源 + MyBatis-Plus + JWT 配置
14. **验证依赖**：检查 admin 模块是否直接声明了 `fireworks-common`、`fireworks-model`、`mybatis-plus-boot-starter`
15. **测试**：生成正确 BCrypt 哈希 → 更新 DB → curl 验证登录

---

## 九、今日补充（2026-03-12）：Redis 会话 + ThreadLocal + 安全响应定制

> 内容：MySQL 连接修复、日志配置、Spring Security 403 流程、自定义 401/403 响应、Redis 集成、登录流程 Redis + ThreadLocal 改造

### 9.1 常见问题与快速修复

#### 9.1.1 MySQL 8.x：`Public Key Retrieval is not allowed`

**现象**：首次连接 MySQL 报错 `SQLNonTransientConnectionException: Public Key Retrieval is not allowed`。

**原因**：MySQL 8 默认使用 `caching_sha2_password`，JDBC 驱动需从服务端获取公钥加密密码，但默认不允许。

**修复**：在 JDBC URL 末尾追加 `&allowPublicKeyRetrieval=true`。

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fireworks_neighborhood?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
```

#### 9.1.2 控制台无日志输出

**原因**：缺少日志级别配置，`log.debug` 默认不输出。

**修复**：在 `application.yml` 中增加 `logging.level`：

```yaml
logging:
  level:
    com.fireworks: debug
    org.springframework.security: warn
    com.baomidou.mybatisplus: debug
```

#### 9.1.3 Jackson 依赖 groupId 错误

**现象**：`Could not find com.fasterxml.jackson.databind:jackson-databind:.`（版本号为空）。

**原因**：groupId 错误，应为 `com.fasterxml.jackson.core`，不是 `com.fasterxml.jackson.databind`。

**修复**：

```groovy
implementation 'com.fasterxml.jackson.core:jackson-databind'
```

### 9.2 Spring Security 请求生命周期与 403 无日志原因

当请求 `GET /admin/demo`（未携带 Token）返回 403 且控制台无输出时，请求在到达 Controller 之前就被 Security 过滤器链拦截：

```
HTTP 请求
  → JwtAuthenticationTokenFilter（无 Token 时直接 doFilter，无日志）
  → FilterSecurityInterceptor（发现未认证，抛 AccessDeniedException）
  → ExceptionTranslationFilter（委托 AuthenticationEntryPoint 返回 403）
  → 客户端收到 403，不会进入 Controller
```

**结论**：`@ExceptionHandler` 捕获不到此类异常，因为异常发生在 DispatcherServlet 之前的过滤器链中。

### 9.3 自定义 401/403 响应（统一 Result 格式）

为替代 Spring Security 默认的 `{"status":403,"error":"Forbidden"}`，需配置自定义处理器：

| 处理器 | 触发场景 | 响应 |
|--------|----------|------|
| `AuthenticationEntryPoint` | 未认证（无 Token / Token 无效） | 401 |
| `AccessDeniedHandler` | 已认证但无权限 | 403 |

**实现要点**：

- 新建 `RestAuthenticationEntryPoint`、`RestAccessDeniedHandler`
- 使用 `Result.unauthorized()` / `Result.forbidden()` 配合 `ObjectMapper` 写 JSON
- 在 `SecurityConfig` 中通过 `.exceptionHandling().authenticationEntryPoint(...).accessDeniedHandler(...)` 注册

**注意**：`.httpBasic().disable()` 返回的是 `HttpSecurity`，其后不可再链 `.and()`，应直接接 `.exceptionHandling()`。

### 9.4 Redis 集成

#### 9.4.1 依赖与配置

- **依赖**：在 `fireworks-service/build.gradle` 中增加 `spring-boot-starter-data-redis`
- **配置**（`fireworks-admin/application.yml`）：

```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6378
    # password 无则省略
```

#### 9.4.2 RedisConfig（Bean + 序列化）

路径：`fireworks-service/src/main/java/com/fireworks/service/config/RedisConfig.java`

- 定义 `RedisTemplate<String, Object>`
- Key：`StringRedisSerializer`
- Value：`Jackson2JsonRedisSerializer`，替代默认 JDK 序列化

#### 9.4.3 RedisUtil（静态方法泛型存取）

路径：`fireworks-service/src/main/java/com/fireworks/service/utils/RedisUtil.java`

- 使用 `@PostConstruct` 将 `StringRedisTemplate` 注入到静态变量
- 提供静态方法：`set(k,v)`、`set(k,v,timeout,unit)`、`get(k,Class)`、`delete`、`expire` 等
- 以 JSON 序列化对象，支持泛型反序列化

**使用示例**：

```java
RedisUtil.set("user:1", userObj);
RedisUtil.set("token:abc", tokenObj, 30, TimeUnit.MINUTES);
User user = RedisUtil.get("user:1", User.class);
```

### 9.5 登录流程 Redis + ThreadLocal 改造

#### 9.5.1 改造目标

- Token 用户信息写入 Redis，支持修改密码后强制下线
- 请求期间用户信息放入 ThreadLocal，业务层直接使用，无需再查 DB/Redis

#### 9.5.2 改造后的认证流程

```
登录 POST /admin/login
  └─ UmsAdminServiceImpl.login()
       ├─ 校验密码
       ├─ JwtTokenUtil.generateToken()
       └─ RedisUtil.set(USER_INFO_KEY + username, AdminUserDetails, 30, MINUTES)

后续请求（带 Token）
  └─ JwtAuthenticationTokenFilter
       ├─ 解析 Token 取 username
       ├─ RedisUtil.get(USER_INFO_KEY + username, AdminUserDetails.class)  ← 不查 DB
       ├─ Redis 无数据 → 视为会话过期/强制下线，放行后由后续过滤器返回 401
       ├─ Redis 有数据 → 校验 Token、写入 SecurityContext + UserDetailsThreadLocal
       ├─ RedisUtil.expire(...) 刷新 TTL（滑动窗口）
       └─ finally: UserDetailsThreadLocal.removeUserDetails()  ← 防止内存泄漏
```

#### 9.5.3 关键设计点

| 组件 | 说明 |
|------|------|
| `AdminUserDetails` | 移至 `fireworks-common`，需可被 Jackson 反序列化：去掉 `final`、增加无参构造和 setter、`UserDetails` 接口方法加 `@JsonIgnore` |
| `UserDetailsThreadLocal` | 存储当前请求的用户信息，`setUserDetails` / `getUserDetails` / `removeUserDetails` |
| `RedisKeyConstant.USER_INFO_KEY` | 如 `user:info:`，与 username 拼接构成 key |
| JWT 过期 vs Redis TTL | JWT 过期（如 24h）为绝对上限；Redis TTL（如 30min）控制会话活跃期，每次请求刷新 |

#### 9.5.4 强制下线实现

修改密码后执行：

```java
RedisUtil.delete(RedisKeyConstant.USER_INFO_KEY + username);
```

下次请求从 Redis 取不到用户即返回 401。

#### 9.5.5 fireworks-common 新增依赖

```groovy
implementation project(':fireworks-model')
implementation 'org.springframework.security:spring-security-core'
implementation 'com.fasterxml.jackson.core:jackson-annotations'
```

#### 9.5.6 常见坑点（已修复）

1. **ThreadLocal 泄漏**：必须在 `finally` 中调用 `removeUserDetails()`，否则线程池复用会导致数据串请求
2. **AdminUserDetails 反序列化失败**：不能使用 `final` 字段且必须有空参构造
3. **`UserDetails.class` 反序列化**：接口无法实例化，应用 `AdminUserDetails.class`
4. **过滤器仍查 DB**：应从 Redis 读用户信息，否则 Redis 缓存无效

### 9.6 Redis 中未查到用户时的处理方式

当 Redis 中无对应用户时，有两种等价方式，均可得到 401：

| 方式 | 流程 | 特点 |
|------|------|------|
| **继续 filterChain.doFilter** | 不设置 SecurityContext → FilterSecurityInterceptor 抛 AccessDeniedException → ExceptionTranslationFilter 委托 AuthenticationEntryPoint → 401 | 符合 Spring Security 默认设计 |
| **直接抛 AuthenticationException** | 如 `InsufficientAuthenticationException` → ExceptionTranslationFilter 捕获 → AuthenticationEntryPoint → 401 | 语义明确、 fail fast |

两种方式都依赖 `try-finally` 中的 `UserDetailsThreadLocal.removeUserDetails()` 做清理。

