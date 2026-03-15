-- 存量表迁移：为 pms_product 新增 stock 字段（若表已存在且无 stock 列时执行）
USE fireworks_neighborhood;

ALTER TABLE pms_product
    ADD COLUMN stock INT NOT NULL DEFAULT 0 COMMENT '库存' AFTER price;
