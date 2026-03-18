USE fireworks_neighborhood;

ALTER TABLE pms_product ADD COLUMN IF NOT EXISTS limit_per_user INT DEFAULT NULL
    COMMENT '每人限购数量，NULL 表示不限购';
