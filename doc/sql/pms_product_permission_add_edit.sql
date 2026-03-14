-- ============================================================
-- 烟火邻里 - 商品新增/编辑权限（若已执行 pms_product_init 旧版可单独执行此脚本）
-- ============================================================
USE fireworks_neighborhood;

INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '新增商品', 'pms:product:add', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'pms:product:add' AND type = 2);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '编辑商品', 'pms:product:edit', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'pms:product:edit' AND type = 2);

INSERT INTO ums_role_permission_relation (role_id, permission_id)
SELECT 1, p.id FROM ums_permission p
WHERE p.type = 2 AND p.value IN ('pms:product:add', 'pms:product:edit')
  AND NOT EXISTS (SELECT 1 FROM ums_role_permission_relation rpr WHERE rpr.role_id = 1 AND rpr.permission_id = p.id);
