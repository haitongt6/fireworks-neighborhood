-- ============================================================
-- 烟火邻里 - 后台菜单初始化
-- 字符集：utf8（按项目规范）
-- 执行前请先执行 fireworks_neighborhood.sql 完成基础表与角色初始化
-- 可重复执行：已存在的菜单不重复插入
-- ============================================================

USE fireworks_neighborhood;

-- ------------------------------------------------------------
-- 菜单数据（对应 src/views 下的页面）
-- type: 0-目录, 1-菜单, 2-按钮
-- value: 菜单类型时存储前端路由 path，目录可为空
-- icon: 对应 lucide-vue-next 的图标名
-- ------------------------------------------------------------
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '仪表盘', '/', 'LayoutDashboard', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/' AND type = 1);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '商品管理', '/products', 'Package', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/products' AND type = 1);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '订单管理', '/orders', 'ClipboardList', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/orders' AND type = 1);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '自提点管理', '/points', 'MapPin', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/points' AND type = 1);

-- 系统设置（目录 type=0）：若存在旧的菜单类型，先改为目录；否则新增
UPDATE ums_permission SET type = 0, value = '' WHERE name = '系统设置' AND type = 1;
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '系统设置', '', 'Settings', 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE name = '系统设置' AND type = 0);

-- 用户管理（系统设置子菜单）
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT p.id, '用户管理', '/settings/users', 'Users', 1
FROM ums_permission p WHERE p.name = '系统设置' AND p.type = 0
AND NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/settings/users' AND type = 1);

-- 角色管理（系统设置子菜单）
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT p.id, '角色管理', '/settings/roles', 'Shield', 1
FROM ums_permission p WHERE p.name = '系统设置' AND p.type = 0
AND NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/settings/roles' AND type = 1);

-- 角色管理按钮权限
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '角色列表', 'ums:role:list', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'ums:role:list' AND type = 2);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '新增角色', 'ums:role:add', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'ums:role:add' AND type = 2);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '编辑角色', 'ums:role:edit', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'ums:role:edit' AND type = 2);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '删除角色', 'ums:role:delete', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'ums:role:delete' AND type = 2);

-- 删除旧的独立用户管理 /users（若存在）
DELETE rpr FROM ums_role_permission_relation rpr
  JOIN ums_permission p ON rpr.permission_id = p.id
  WHERE p.value = '/users' AND p.type = 1;
DELETE FROM ums_permission WHERE value = '/users' AND type = 1;

-- 将上述菜单权限授予超级管理员角色（role_id=1）
INSERT INTO ums_role_permission_relation (role_id, permission_id)
SELECT 1, p.id FROM ums_permission p
WHERE (p.type = 1 OR p.type = 0) AND p.value IN ('/', '/products', '/orders', '/points', '/settings/users', '/settings/roles')
  AND NOT EXISTS (SELECT 1 FROM ums_role_permission_relation rpr WHERE rpr.role_id = 1 AND rpr.permission_id = p.id);
-- 角色管理按钮权限授予超级管理员
INSERT INTO ums_role_permission_relation (role_id, permission_id)
SELECT 1, p.id FROM ums_permission p
WHERE p.type = 2 AND p.value IN ('ums:role:list', 'ums:role:add', 'ums:role:edit', 'ums:role:delete')
  AND NOT EXISTS (SELECT 1 FROM ums_role_permission_relation rpr WHERE rpr.role_id = 1 AND rpr.permission_id = p.id);
-- 系统设置目录也需授权（供子菜单展示）
INSERT INTO ums_role_permission_relation (role_id, permission_id)
SELECT 1, p.id FROM ums_permission p
WHERE p.name = '系统设置' AND p.type = 0
  AND NOT EXISTS (SELECT 1 FROM ums_role_permission_relation rpr WHERE rpr.role_id = 1 AND rpr.permission_id = p.id);
