-- ============================================================
-- 烟火邻里 - 商品类目表与菜单初始化
-- 类目设计：一级扁平，无 pid
-- 执行前请先执行 fireworks_neighborhood.sql 和 ums_menu_init.sql
-- ============================================================

USE fireworks_neighborhood;

-- ------------------------------------------------------------
-- 1. 商品类目表（一级扁平）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pms_product_category
(
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name       VARCHAR(100) NOT NULL COMMENT '类目名称',
    sort       INT          NOT NULL DEFAULT 0 COMMENT '排序，升序',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME             COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sort (sort),
    KEY idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品类目表（一级）';

-- ------------------------------------------------------------
-- 2. 菜单与权限：商品管理改为目录，增加类目管理
-- ------------------------------------------------------------
-- 2.1 将商品管理改为目录（type=0）
UPDATE ums_permission SET type = 0, value = '', icon = 'Package'
WHERE name = '商品管理' AND type = 1 AND value = '/products';

-- 2.2 在商品管理下新增「商品列表」「类目管理」子菜单
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT p.id, '商品列表', '/products', 'Package', 1
FROM ums_permission p WHERE p.name = '商品管理' AND p.type = 0
AND NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/products' AND type = 1 AND pid = p.id);

INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT p.id, '类目管理', '/products/categories', 'FolderOpen', 1
FROM ums_permission p WHERE p.name = '商品管理' AND p.type = 0
AND NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/products/categories' AND type = 1 AND pid = p.id);

-- 2.3 类目管理按钮权限
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '类目列表', 'pms:category:list', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'pms:category:list' AND type = 2);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '新增类目', 'pms:category:add', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'pms:category:add' AND type = 2);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '编辑类目', 'pms:category:edit', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'pms:category:edit' AND type = 2);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '删除类目', 'pms:category:delete', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'pms:category:delete' AND type = 2);

-- 2.4 授予超级管理员
INSERT INTO ums_role_permission_relation (role_id, permission_id)
SELECT 1, p.id FROM ums_permission p
WHERE p.value IN ('/products', '/products/categories')
  AND NOT EXISTS (SELECT 1 FROM ums_role_permission_relation rpr WHERE rpr.role_id = 1 AND rpr.permission_id = p.id);

INSERT INTO ums_role_permission_relation (role_id, permission_id)
SELECT 1, p.id FROM ums_permission p
WHERE p.type = 2 AND p.value IN ('pms:category:list', 'pms:category:add', 'pms:category:edit', 'pms:category:delete')
  AND NOT EXISTS (SELECT 1 FROM ums_role_permission_relation rpr WHERE rpr.role_id = 1 AND rpr.permission_id = p.id);
