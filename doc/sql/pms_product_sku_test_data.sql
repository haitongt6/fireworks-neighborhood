-- ============================================================
-- 烟火邻里 - 商品 SKU 测试数据（两条）
-- 对应 pms_product 中两条测试商品（id=1 苹果、id=2 牛奶）
-- 请根据实际 product_id 自行调整
-- ============================================================
USE fireworks_neighborhood;

-- 商品1 苹果：礼盒装 SKU
INSERT INTO pms_product_sku (product_id, spec_values, sku_pic, price, stock, sku_code, status, sort)
VALUES (1, '规格:礼盒装', 'https://picsum.photos/seed/apple/200/200', 59.90, 156, 'SKU-APPLE-001', 1, 0);

-- 商品2 牛奶：12 提 SKU
INSERT INTO pms_product_sku (product_id, spec_values, sku_pic, price, stock, sku_code, status, sort)
VALUES (2, '', 'https://picsum.photos/seed/milk/200/200', 49.90, 85, 'SKU-MILK-001', 1, 0);
