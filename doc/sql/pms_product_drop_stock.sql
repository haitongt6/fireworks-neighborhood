-- ============================================================
-- 烟火邻里 - 移除 pms_product.stock 迁移脚本
-- 库存改为从 pms_product_sku 关联汇总获取
-- 若表为新建（无 stock 列）可跳过
-- ============================================================
USE fireworks_neighborhood;
ALTER TABLE pms_product DROP COLUMN stock;
