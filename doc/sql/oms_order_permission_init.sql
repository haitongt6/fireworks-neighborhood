USE fireworks_neighborhood;

-- 订单相关后台权限
INSERT INTO ums_permission (pid, name, value, type)
VALUES (0, '订单列表', 'oms:order:list', 2);
INSERT INTO ums_role_permission_relation (role_id, permission_id)
VALUES (1, LAST_INSERT_ID());

INSERT INTO ums_permission (pid, name, value, type)
VALUES (0, '订单详情', 'oms:order:detail', 2);
INSERT INTO ums_role_permission_relation (role_id, permission_id)
VALUES (1, LAST_INSERT_ID());

INSERT INTO ums_permission (pid, name, value, type)
VALUES (0, '关闭订单', 'oms:order:close', 2);
INSERT INTO ums_role_permission_relation (role_id, permission_id)
VALUES (1, LAST_INSERT_ID());

INSERT INTO ums_permission (pid, name, value, type)
VALUES (0, '模拟支付成功', 'oms:pay:mock:success', 2);
INSERT INTO ums_role_permission_relation (role_id, permission_id)
VALUES (1, LAST_INSERT_ID());
