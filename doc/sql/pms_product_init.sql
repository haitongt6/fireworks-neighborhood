-- ============================================================
-- 烟火邻里 - 商品管理表设计（商品详情表 + 商品SKU表）
-- 执行前请先执行 fireworks_neighborhood.sql、pms_category_init.sql
-- ============================================================
-- 设计说明：
--   1. 商品ID：BIGINT 自增，与项目现有表(pms_product_category)保持一致，支持大规模数据
--   2. 价格：DECIMAL(10,2)，精确存储金额，单位元，避免 FLOAT 精度问题
--   3. 主图：images 单字段 TEXT，存多张图片URL（JSON数组或逗号分隔）
-- ============================================================

USE fireworks_neighborhood;

-- ------------------------------------------------------------
-- 1. 商品详情表（SPU 维度）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pms_product
(
    id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    title       VARCHAR(200)   NOT NULL COMMENT '商品标题',
    sub_title   VARCHAR(300)   DEFAULT NULL COMMENT '副标题/促销语',
    category_id BIGINT         NOT NULL COMMENT '类目ID，关联 pms_product_category',
    images      TEXT           DEFAULT NULL COMMENT '主图，多张图片URL存JSON数组或逗号分隔',
    main_video  VARCHAR(500)   DEFAULT NULL COMMENT '主图视频URL',
    detail_pics TEXT           DEFAULT NULL COMMENT '详情图，多张图片URL存JSON数组或逗号分隔',
    price       DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '展示价/起售价，单位元',
    status      TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1-上架，0-下架，2-待上架',
    sort        INT            NOT NULL DEFAULT 0 COMMENT '排序，升序',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_sort (sort),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品详情表（SPU）';

-- ------------------------------------------------------------
-- 2. 商品SKU表（规格维度，一对多关联商品）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pms_product_sku
(
    id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    product_id  BIGINT         NOT NULL COMMENT '商品ID，关联 pms_product',
    spec_values VARCHAR(200)   NOT NULL DEFAULT '' COMMENT '规格值，如：颜色:红,尺码:L',
    sku_pic     VARCHAR(500)   DEFAULT NULL COMMENT 'SKU 主图（规格图）',
    price       DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'SKU 售价，单位元',
    stock       INT            NOT NULL DEFAULT 0 COMMENT 'SKU 库存',
    sku_code    VARCHAR(64)    DEFAULT NULL COMMENT '商家编码/SKU编码',
    status      TINYINT        NOT NULL DEFAULT 1 COMMENT '上下架：1-上架，0-下架',
    sort        INT            NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_product_id (product_id),
    KEY idx_status (status),
    KEY idx_sku_code (sku_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品SKU表（规格）';

-- ------------------------------------------------------------
-- 3. 商品权限与授权
-- ------------------------------------------------------------
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '商品列表', 'pms:product:list', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'pms:product:list' AND type = 2);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '新增商品', 'pms:product:add', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'pms:product:add' AND type = 2);
INSERT INTO ums_permission (pid, name, value, icon, type)
SELECT 0, '编辑商品', 'pms:product:edit', NULL, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ums_permission WHERE value = 'pms:product:edit' AND type = 2);

INSERT INTO ums_role_permission_relation (role_id, permission_id)
SELECT 1, p.id FROM ums_permission p
WHERE p.type = 2 AND p.value IN ('pms:product:list', 'pms:product:add', 'pms:product:edit')
  AND NOT EXISTS (SELECT 1 FROM ums_role_permission_relation rpr WHERE rpr.role_id = 1 AND rpr.permission_id = p.id);

-- ------------------------------------------------------------
-- 4. 存量表迁移
-- 若表已存在且含 stock 列，请先执行 doc/sql/pms_product_drop_stock.sql

-- ------------------------------------------------------------
-- 5. 测试商品数据（可选，便于联调；库存从 pms_product_sku 关联获取）
-- ------------------------------------------------------------
INSERT INTO pms_product (title, sub_title, category_id, images, price, status, sort)
SELECT '新鲜红富士苹果 5kg 礼盒装', '顺丰包邮', c.id, 'https://picsum.photos/seed/apple/200/200', 59.90, 1, 0
FROM pms_product_category c LIMIT 1;
INSERT INTO pms_product (title, sub_title, category_id, images, price, status, sort)
SELECT '特仑苏纯牛奶 250ml*12 提', '品质之选', c.id, 'https://picsum.photos/seed/milk/200/200', 49.90, 1, 1
FROM pms_product_category c LIMIT 1;
