-- ============================================================
-- 订单 / 支付 / 锁库存 初始化脚本
-- 数据库：fireworks_neighborhood
-- 字符集：utf8mb4
-- ============================================================

USE fireworks_neighborhood;

-- ------------------------------------------------------------
-- 1. 商品表补充字段：锁定库存、销量
-- ------------------------------------------------------------
ALTER TABLE pms_product
    ADD COLUMN lock_stock INT NOT NULL DEFAULT 0 COMMENT '锁定库存（已下单未支付）' AFTER stock,
    ADD COLUMN sale INT NOT NULL DEFAULT 0 COMMENT '销量' AFTER lock_stock;

-- ------------------------------------------------------------
-- 2. 订单主表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS oms_order
(
    id                      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no                VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id                 BIGINT NOT NULL COMMENT '用户ID',
    order_status            TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已取消，3-已关闭，4-已完成',
    pay_status              TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-支付中，2-支付成功，3-支付失败，4-已关闭',
    source_type             TINYINT NOT NULL DEFAULT 1 COMMENT '订单来源：1-购物车下单，2-立即购买',
    total_amount            DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
    pay_amount              DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
    item_count              INT NOT NULL DEFAULT 0 COMMENT '商品总件数',
    receiver_name           VARCHAR(64) DEFAULT NULL COMMENT '收货人',
    receiver_phone          VARCHAR(32) DEFAULT NULL COMMENT '收货电话',
    receiver_province       VARCHAR(64) DEFAULT NULL COMMENT '省',
    receiver_city           VARCHAR(64) DEFAULT NULL COMMENT '市',
    receiver_district       VARCHAR(64) DEFAULT NULL COMMENT '区',
    receiver_detail_address VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    remark                  VARCHAR(255) DEFAULT NULL COMMENT '用户备注',
    submit_token            VARCHAR(64) DEFAULT NULL COMMENT '提交幂等token',
    expire_time             DATETIME DEFAULT NULL COMMENT '支付过期时间',
    pay_time                DATETIME DEFAULT NULL COMMENT '支付成功时间',
    cancel_time             DATETIME DEFAULT NULL COMMENT '取消时间',
    cancel_reason           VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    deleted                 TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-否，1-是',
    create_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_submit_token (submit_token),
    KEY idx_user_status (user_id, order_status),
    KEY idx_user_create_time (user_id, create_time),
    KEY idx_expire_time (expire_time),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单主表';

-- ------------------------------------------------------------
-- 3. 订单明细表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS oms_order_item
(
    id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id      BIGINT NOT NULL COMMENT '订单ID',
    order_no      VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id       BIGINT NOT NULL COMMENT '用户ID',
    product_id    BIGINT NOT NULL COMMENT '商品ID',
    product_title VARCHAR(255) NOT NULL COMMENT '商品标题快照',
    product_image VARCHAR(500) DEFAULT NULL COMMENT '商品图片快照',
    product_price DECIMAL(10,2) NOT NULL COMMENT '商品单价快照',
    quantity      INT NOT NULL COMMENT '购买数量',
    total_amount  DECIMAL(10,2) NOT NULL COMMENT '明细小计金额',
    item_status   TINYINT NOT NULL DEFAULT 0 COMMENT '明细状态：0-正常，1-已取消',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_product_id (product_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单明细表';

-- ------------------------------------------------------------
-- 4. 支付单表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS oms_order_pay
(
    id                   BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    pay_order_no         VARCHAR(32) NOT NULL COMMENT '支付单号',
    order_id             BIGINT NOT NULL COMMENT '订单ID',
    order_no             VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id              BIGINT NOT NULL COMMENT '用户ID',
    pay_type             TINYINT NOT NULL DEFAULT 9 COMMENT '支付方式：1-微信，2-支付宝，9-模拟支付',
    pay_status           TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-支付中，2-支付成功，3-支付失败，4-已关闭',
    pay_amount           DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    third_party_trade_no VARCHAR(64) DEFAULT NULL COMMENT '第三方交易号，模拟支付可使用MOCK开头',
    request_no           VARCHAR(64) DEFAULT NULL COMMENT '支付请求幂等号',
    pay_time             DATETIME DEFAULT NULL COMMENT '支付成功时间',
    fail_reason          VARCHAR(255) DEFAULT NULL COMMENT '支付失败原因',
    notify_status        TINYINT NOT NULL DEFAULT 0 COMMENT '通知处理状态：0-未处理，1-已处理',
    create_time          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pay_order_no (pay_order_no),
    UNIQUE KEY uk_request_no (request_no),
    KEY idx_order_id (order_id),
    KEY idx_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单支付单表';

-- ------------------------------------------------------------
-- 5. 锁库存表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS oms_order_stock_lock
(
    id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id      BIGINT NOT NULL COMMENT '订单ID',
    order_no      VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id       BIGINT NOT NULL COMMENT '用户ID',
    product_id    BIGINT NOT NULL COMMENT '商品ID',
    lock_quantity INT NOT NULL COMMENT '锁定数量',
    lock_status   TINYINT NOT NULL DEFAULT 0 COMMENT '锁库存状态：0-已锁定，1-已扣减，2-已释放',
    expire_time   DATETIME NOT NULL COMMENT '锁库存过期时间',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_product_id (product_id),
    KEY idx_expire_time (expire_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单锁库存表';

-- ------------------------------------------------------------
-- 6. 订单操作日志表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS oms_order_operate_log
(
    id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id      BIGINT NOT NULL COMMENT '订单ID',
    order_no      VARCHAR(32) NOT NULL COMMENT '订单号',
    operate_type  TINYINT NOT NULL COMMENT '操作类型：1-提交订单，2-发起支付，3-支付成功，4-支付失败，5-用户取消订单，6-超时关闭订单，7-释放锁库存，8-后台关闭订单',
    pre_status    VARCHAR(64) DEFAULT NULL COMMENT '变更前状态',
    post_status   VARCHAR(64) DEFAULT NULL COMMENT '变更后状态',
    note          VARCHAR(255) DEFAULT NULL COMMENT '备注',
    operator_id   BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_type TINYINT NOT NULL DEFAULT 1 COMMENT '操作人类型：1-用户，2-系统，3-后台管理员',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_order_no (order_no),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单操作日志表';
