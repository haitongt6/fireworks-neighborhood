-- ============================================================
-- 烟火邻里 - 后台菜单初始化
-- 字符集：utf8（按项目规范）
-- 执行前请先执行 fireworks_neighborhood.sql 完成基础表与角色初始化
-- 可重复执行：已存在的菜单不重复插入，关联关系用 IGNORE 避免重复
-- ============================================================

USE fireworks_neighborhood;

-- ------------------------------------------------------------
-- 菜单数据（对应 src/views 下的页面）
-- type: 0-目录, 1-菜单, 2-按钮
-- value: 菜单类型时存储前端路由 path
-- icon: 对应 lucide-vue-next 的图标名
-- ------------------------------------------------------------
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '仪表盘', '/', 'LayoutDashboard', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/' AND type = 1);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '商品管理', '/products', 'Package', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/products' AND type = 1);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '订单管理', '/orders', 'ClipboardList', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/orders' AND type = 1);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '用户管理', '/users', 'Users', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/users' AND type = 1);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '自提点管理', '/points', 'MapPin', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/points' AND type = 1);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '系统设置', '/settings', 'Settings', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/settings' AND type = 1);

-- 将上述菜单权限授予超级管理员角色（role_id=1）
INSERT INTO ums_role_permission_relation (role_id, permission_id)
SELECT 1, p.id FROM ums_permission p
WHERE p.type = 1 AND p.value IN ('/', '/products', '/orders', '/users', '/points', '/settings')
  AND NOT EXISTS (SELECT 1 FROM ums_role_permission_relation rpr WHERE rpr.role_id = 1 AND rpr.permission_id = p.id);
