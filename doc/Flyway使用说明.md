# Flyway 使用说明

## 一、Flyway 是什么

**Flyway** 是一个开源的**数据库版本控制/迁移工具**，用于管理数据库 Schema 的演进。

### 核心概念

| 概念 | 说明 |
|------|------|
| **Migration（迁移）** | 一个 SQL 或 Java 文件，代表一次数据库变更 |
| **Version** | 迁移的版本号，如 `V1`、`V2`、`V2.1` |
| **flyway_schema_history** | Flyway 在目标库中创建的表，记录已执行的迁移及其校验和 |
| **Baseline（基线）** | 对已有数据库“打标记”，表示此前的变更不再由 Flyway 管理 |

### 工作流程

```
应用启动 → Flyway 读取 db/migration 下的脚本 → 与 flyway_schema_history 比对
    → 未执行的迁移按版本号顺序执行 → 执行完成后应用正常启动
```

---

## 二、本项目的 Flyway 配置

### 1. 依赖位置

`fireworks-admin/build.gradle` 中已添加：

```groovy
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-mysql'
```

### 2. 配置项（application.yml）

```yaml
spring:
  flyway:
    enabled: true                    # 启用 Flyway
    baseline-on-migrate: true       # 已有库且无历史表时，自动建立基线后执行迁移
    baseline-version: 0             # 基线版本，便于 V1 等在首次接入时正常执行
    locations: classpath:db/migration  # 迁移脚本所在目录
```

### 3. 迁移脚本目录

```
fireworks-admin/src/main/resources/
└── db/
    └── migration/
        └── V1__add_button_permissions.sql
```

---

## 三、如何新增接口权限（标准流程）

当你在 Controller 中新增一个需要鉴权的接口时，按以下三步操作：

### 步骤 1：在 PermissionConstant 中新增常量

文件：`fireworks-model/src/main/java/com/fireworks/model/constant/PermissionConstant.java`

```java
/** 删除管理员 */
public static final String ADMIN_DELETE = "ums:admin:delete";
```

### 步骤 2：新增 Flyway 迁移脚本

在 `db/migration/` 下创建新文件，**版本号必须递增**，例如：

**V2__add_admin_delete_permission.sql**

```sql
-- 插入新按钮权限
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '删除管理员', 'ums:admin:delete', NULL, 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'ums:admin:delete' AND type = 2);

-- 授予超级管理员
INSERT INTO ums_role_permission_relation (role_id, permission_id)
SELECT 1, p.id FROM ums_permission p
WHERE p.type = 2 AND p.value = 'ums:admin:delete'
  AND NOT EXISTS (SELECT 1 FROM ums_role_permission_relation rpr WHERE rpr.role_id = 1 AND rpr.permission_id = p.id);
```

### 步骤 3：在 Controller 使用

```java
@DeleteMapping("/user/{adminId}")
@PreAuthorize("hasAuthority(T(com.fireworks.model.constant.PermissionConstant).ADMIN_DELETE)")
public Result<Void> deleteUser(@PathVariable Long adminId) {
    // ...
}
```

---

## 四、迁移脚本命名规范

| 格式 | 示例 | 说明 |
|------|------|------|
| `V{版本}__{描述}.sql` | `V1__add_button_permissions.sql` | 版本号与描述用双下划线分隔 |
| 版本号 | `V1`、`V2`、`V2_1` | 数字，可用下划线分隔子版本 |

**注意**：版本号一旦执行过，**不要修改**该脚本内容，否则 Flyway 会因校验和不匹配而报错。后续变更应写在新的 `V3`、`V4` 等脚本中。

---

## 五、常见场景

### 1. 首次启动（数据库为空或已有表）

- **空库**：先执行 `doc/sql/fireworks_neighborhood.sql` 创建表结构，再启动应用，Flyway 会执行 V1。
- **已有库**：配置了 `baseline-on-migrate: true`，Flyway 会创建 `flyway_schema_history` 并建立基线，然后执行 V1 及之后的迁移。

### 2. 脚本执行失败

- 检查控制台报错，定位失败的 SQL。
- 修复脚本后，需要**手动**在数据库中删除 `flyway_schema_history` 里对应失败记录的条目，或使用 Flyway 的 repair 命令（如有需要）。
- 再次启动应用，Flyway 会重试该迁移。

### 3. 已执行的脚本不要改

- 若必须调整，应新建更高版本的迁移脚本，在新区块中做修正（如 `UPDATE`、补充 `INSERT` 等）。
- 禁止修改已执行过的脚本内容，否则会导致校验失败。

---

## 六、与权限常量的一致性

| 环节 | 职责 |
|------|------|
| **PermissionConstant** | 代码中权限标识的唯一来源，避免硬编码字符串 |
| **Flyway 迁移** | 保证 `ums_permission` 表中有对应记录，并为角色授权 |
| **@PreAuthorize** | 引用常量，如 `hasAuthority(T(...).ADMIN_LIST)` |

新增权限时，务必同时维护这三处，保持代码与数据库一致。
