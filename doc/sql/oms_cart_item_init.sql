USE fireworks_neighborhood;

CREATE TABLE IF NOT EXISTS oms_cart_item (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    user_id         BIGINT        NOT NULL,
    product_id      BIGINT        NOT NULL,
    quantity        INT           NOT NULL DEFAULT 1,
    price_snapshot  DECIMAL(10,2) NOT NULL,
    title_snapshot  VARCHAR(200)           DEFAULT NULL,
    image_snapshot  VARCHAR(500)           DEFAULT NULL,
    create_time     DATETIME               DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_product (user_id, product_id),
    KEY idx_user_id (user_id),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车明细';
