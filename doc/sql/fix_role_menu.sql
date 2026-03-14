-- ============================================================
-- 修复：确保角色管理菜单对超级管理员可见
-- 执行后请重新登录或清除 Redis 缓存
-- ============================================================

USE fireworks_neighborhood;

-- 1. 确保系统设置目录存在（type=0）
UPDATE ums_permission SET type = 0, value = '' WHERE name = '系统设置';
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '系统设置', '', 'Settings', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE name = '系统设置' AND type = 0);

-- 2. 插入角色管理菜单（系统设置子菜单）
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT p.id, '角色管理', '/settings/roles', 'Shield', 1
FROM ums_permission p
WHERE p.name = '系统设置' AND p.type = 0
  AND NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = '/settings/roles' AND type = 1);

-- 3. 将角色管理菜单授权给超级管理员（role_id=1）
INSERT INTO ums_role_permission_relation (role_id, permission_id)
SELECT 1, p.id FROM ums_permission p
WHERE p.value = '/settings/roles' AND p.type = 1
  AND NOT EXISTS (SELECT 1 FROM ums_role_permission_relation rpr WHERE rpr.role_id = 1 AND rpr.permission_id = p.id);

-- 4. 确保系统设置目录也授权给超级管理员（用于展示子菜单）
INSERT INTO ums_role_permission_relation (role_id, permission_id)
SELECT 1, p.id FROM ums_permission p
WHERE p.name = '系统设置' AND p.type = 0
  AND NOT EXISTS (SELECT 1 FROM ums_role_permission_relation rpr WHERE rpr.role_id = 1 AND rpr.permission_id = p.id);
