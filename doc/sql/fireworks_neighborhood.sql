-- ============================================================
-- 烟火邻里社区团购系统 - 数据库初始化脚本
-- 数据库：fireworks_neighborhood
-- 字符集：utf8mb4（支持 emoji）
-- ============================================================

CREATE DATABASE IF NOT EXISTS fireworks_neighborhood
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE fireworks_neighborhood;

-- ------------------------------------------------------------
-- 1. 管理员表
-- ------------------------------------------------------------
CREATE TABLE ums_admin
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(64)  NOT NULL COMMENT '登录用户名，全局唯一',
    password    VARCHAR(128) NOT NULL COMMENT 'BCrypt 加密密码，禁止明文存储',
    nickname    VARCHAR(64)           COMMENT '显示昵称',
    email       VARCHAR(128)          COMMENT '联系邮箱',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：1-启用，0-禁用',
    create_time DATETIME              COMMENT '账号创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='后台管理员表';

-- ------------------------------------------------------------
-- 2. 角色表
-- ------------------------------------------------------------
CREATE TABLE ums_role
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(100) NOT NULL COMMENT '角色名称，如 SUPER_ADMIN',
    description VARCHAR(500)          COMMENT '角色描述',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='后台角色表';

-- ------------------------------------------------------------
-- 3. 权限表（树形结构）
-- ------------------------------------------------------------
CREATE TABLE ums_permission
(
    id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    pid   BIGINT       NOT NULL DEFAULT 0 COMMENT '父权限 ID，0 表示根节点',
    name  VARCHAR(200) NOT NULL COMMENT '权限名称（界面展示用）',
    value VARCHAR(200)          COMMENT '权限标识符，如 pms:product:read',
    icon  VARCHAR(500)          COMMENT '菜单图标',
    type  TINYINT      NOT NULL DEFAULT 1 COMMENT '节点类型：0-目录，1-菜单，2-按钮',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='后台权限表';

-- ------------------------------------------------------------
-- 4. 管理员-角色 关联表（多对多）
-- ------------------------------------------------------------
CREATE TABLE ums_admin_role_relation
(
    id       BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    admin_id BIGINT NOT NULL COMMENT '管理员 ID',
    role_id  BIGINT NOT NULL COMMENT '角色 ID',
    PRIMARY KEY (id),
    KEY idx_admin_id (admin_id),
    KEY idx_role_id (role_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='管理员角色关联表';

-- ------------------------------------------------------------
-- 5. 角色-权限 关联表（多对多）
-- ------------------------------------------------------------
CREATE TABLE ums_role_permission_relation
(
    id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id       BIGINT NOT NULL COMMENT '角色 ID',
    permission_id BIGINT NOT NULL COMMENT '权限 ID',
    PRIMARY KEY (id),
    KEY idx_role_id (role_id),
    KEY idx_permission_id (permission_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色权限关联表';

-- ============================================================
-- 初始化测试数据
-- 默认管理员账号：admin / 123456
-- 密码 BCrypt 加密值（rounds=10，由 BCryptPasswordEncoder 实际生成并验证）
-- ============================================================
INSERT INTO ums_admin (username, password, nickname, status, create_time)
VALUES ('admin', '$2a$10$CtDBPecYDAXFkA8i3FVkK.s36ZeJcsIQkmK83g6FJo2kzCxrBAV62', '超级管理员', 1, NOW());

INSERT INTO ums_role (name, description, status)
VALUES ('SUPER_ADMIN', '超级管理员，拥有所有权限', 1);

-- 绑定管理员与角色
INSERT INTO ums_admin_role_relation (admin_id, role_id)
VALUES (1, 1);

-- 添加用户管理权限（供超级管理员添加用户接口使用）
INSERT INTO ums_permission (pid, name, value, type)
VALUES (0, '添加管理员', 'ums:admin:add', 2);

-- 将 ums:admin:add 权限授予超级管理员角色
INSERT INTO ums_role_permission_relation (role_id, permission_id)
VALUES (1, LAST_INSERT_ID());
