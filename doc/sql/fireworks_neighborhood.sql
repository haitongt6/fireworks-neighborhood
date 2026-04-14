/*
 Navicat Premium Data Transfer

 Source Server         : localhost-mysql-8.0.26
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : localhost:3306
 Source Schema         : fireworks_neighborhood

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 12/04/2026 07:45:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for flyway_schema_history
-- ----------------------------
DROP TABLE IF EXISTS `flyway_schema_history`;
CREATE TABLE `flyway_schema_history`  (
  `installed_rank` int NOT NULL,
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `script` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `checksum` int NULL DEFAULT NULL,
  `installed_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`) USING BTREE,
  INDEX `flyway_schema_history_s_idx`(`success` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of flyway_schema_history
-- ----------------------------
INSERT INTO `flyway_schema_history` VALUES (1, '0', '<< Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>', NULL, 'root', '2026-03-14 09:07:27', 0, 1);
INSERT INTO `flyway_schema_history` VALUES (2, '1', 'add button permissions', 'SQL', 'V1__add_button_permissions.sql', -1785412657, 'root', '2026-03-14 09:07:28', 7, 1);
INSERT INTO `flyway_schema_history` VALUES (3, '2', 'link button permissions to menu', 'SQL', 'V2__link_button_permissions_to_menu.sql', -1709999711, 'root', '2026-03-14 09:35:23', 20, 0);

-- ----------------------------
-- Table structure for oms_cart_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_cart_item`;
CREATE TABLE `oms_cart_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `quantity` int NOT NULL DEFAULT 1,
  `price_snapshot` decimal(10, 2) NOT NULL,
  `title_snapshot` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `image_snapshot` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_product`(`user_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_cart_item
-- ----------------------------
INSERT INTO `oms_cart_item` VALUES (29, 1, 2, 28, 49.90, '特仑苏纯牛奶 250ml*12 提', 'https://picsum.photos/seed/milk/200/200', '2026-03-29 21:43:08', '2026-04-01 21:43:55');

-- ----------------------------
-- Table structure for oms_order
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已取消，3-已关闭，4-已完成',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-支付中，2-支付成功，3-支付失败，4-已关闭',
  `source_type` tinyint NOT NULL DEFAULT 1 COMMENT '订单来源：1-购物车下单，2-立即购买',
  `total_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `pay_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  `item_count` int NOT NULL DEFAULT 0 COMMENT '商品总件数',
  `receiver_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人',
  `receiver_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货电话',
  `receiver_province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省',
  `receiver_city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '市',
  `receiver_district` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区',
  `receiver_detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户备注',
  `submit_token` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '提交幂等token',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '支付过期时间',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付成功时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-否，1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_submit_token`(`submit_token` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `order_status` ASC) USING BTREE,
  INDEX `idx_user_create_time`(`user_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order
-- ----------------------------
INSERT INTO `oms_order` VALUES (1, '20260329213505000001', 1, 1, 2, 1, 149.70, 149.70, 3, '1', '1', '1', '1', '1', '1', NULL, '9989150eba954f9aaa775978b15f9643', '2026-03-29 21:50:06', '2026-03-29 21:38:22', NULL, NULL, 0, '2026-03-29 21:35:05', '2026-03-29 21:38:22');
INSERT INTO `oms_order` VALUES (2, '20260329214037000002', 1, 2, 4, 1, 49.90, 49.90, 1, '2', '2', '2', '2', '2', '2', NULL, 'cb4a9e77823a44e598f8147aeace2e40', '2026-03-29 21:55:38', NULL, '2026-03-29 21:41:34', NULL, 0, '2026-03-29 21:40:37', '2026-03-29 21:41:33');

-- ----------------------------
-- Table structure for oms_order_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品标题快照',
  `product_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片快照',
  `product_price` decimal(10, 2) NOT NULL COMMENT '商品单价快照',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '明细小计金额',
  `item_status` tinyint NOT NULL DEFAULT 0 COMMENT '明细状态：0-正常，1-已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_item
-- ----------------------------
INSERT INTO `oms_order_item` VALUES (1, 1, '20260329213505000001', 1, 2, '特仑苏纯牛奶 250ml*12 提', 'https://picsum.photos/seed/milk/200/200', 49.90, 3, 149.70, 0, '2026-03-29 21:35:05', '2026-03-29 21:35:05');
INSERT INTO `oms_order_item` VALUES (2, 2, '20260329214037000002', 1, 2, '特仑苏纯牛奶 250ml*12 提', 'https://picsum.photos/seed/milk/200/200', 49.90, 1, 49.90, 0, '2026-03-29 21:40:37', '2026-03-29 21:40:37');

-- ----------------------------
-- Table structure for oms_order_operate_log
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_operate_log`;
CREATE TABLE `oms_order_operate_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `operate_type` tinyint NOT NULL COMMENT '操作类型：1-提交订单，2-发起支付，3-支付成功，4-支付失败，5-用户取消订单，6-超时关闭订单，7-释放锁库存，8-后台关闭订单',
  `pre_status` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更前状态',
  `post_status` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更后状态',
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_type` tinyint NOT NULL DEFAULT 1 COMMENT '操作人类型：1-用户，2-系统，3-后台管理员',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_operate_log
-- ----------------------------
INSERT INTO `oms_order_operate_log` VALUES (1, 1, '20260329213505000001', 1, NULL, '待支付', '提交订单', 1, 1, '2026-03-29 21:35:05');
INSERT INTO `oms_order_operate_log` VALUES (2, 1, '20260329213505000001', 3, '待支付', '已支付', '支付成功', 1, 1, '2026-03-29 21:38:22');
INSERT INTO `oms_order_operate_log` VALUES (3, 2, '20260329214037000002', 1, NULL, '待支付', '提交订单', 1, 1, '2026-03-29 21:40:37');
INSERT INTO `oms_order_operate_log` VALUES (4, 2, '20260329214037000002', 5, '待支付', '已取消', '用户取消订单', 1, 1, '2026-03-29 21:41:33');

-- ----------------------------
-- Table structure for oms_order_pay
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_pay`;
CREATE TABLE `oms_order_pay`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pay_order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付单号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `pay_type` tinyint NOT NULL DEFAULT 9 COMMENT '支付方式：1-微信，2-支付宝，9-模拟支付',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-支付中，2-支付成功，3-支付失败，4-已关闭',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `third_party_trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '第三方交易号，模拟支付可使用MOCK开头',
  `request_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付请求幂等号',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付成功时间',
  `fail_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付失败原因',
  `notify_status` tinyint NOT NULL DEFAULT 0 COMMENT '通知处理状态：0-未处理，1-已处理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pay_order_no`(`pay_order_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_request_no`(`request_no` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单支付单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_pay
-- ----------------------------
INSERT INTO `oms_order_pay` VALUES (1, 'P20260329213505000001', 1, '20260329213505000001', 1, 9, 2, 149.70, 'MOCK1774791502337', 'REQ1774791502321', '2026-03-29 21:38:22', NULL, 1, '2026-03-29 21:35:05', '2026-03-29 21:38:22');
INSERT INTO `oms_order_pay` VALUES (2, 'P20260329214037000002', 2, '20260329214037000002', 1, 9, 4, 49.90, NULL, NULL, NULL, NULL, 0, '2026-03-29 21:40:37', '2026-03-29 21:41:33');

-- ----------------------------
-- Table structure for oms_order_stock_lock
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_stock_lock`;
CREATE TABLE `oms_order_stock_lock`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `lock_quantity` int NOT NULL COMMENT '锁定数量',
  `lock_status` tinyint NOT NULL DEFAULT 0 COMMENT '锁库存状态：0-已锁定，1-已扣减，2-已释放',
  `expire_time` datetime NOT NULL COMMENT '锁库存过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单锁库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_stock_lock
-- ----------------------------
INSERT INTO `oms_order_stock_lock` VALUES (1, 1, '20260329213505000001', 1, 2, 3, 1, '2026-03-29 21:50:06', '2026-03-29 21:35:05', '2026-03-29 21:38:22');
INSERT INTO `oms_order_stock_lock` VALUES (2, 2, '20260329214037000002', 1, 2, 1, 2, '2026-03-29 21:55:38', '2026-03-29 21:40:37', '2026-03-29 21:41:33');

-- ----------------------------
-- Table structure for pms_product
-- ----------------------------
DROP TABLE IF EXISTS `pms_product`;
CREATE TABLE `pms_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品标题',
  `sub_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '副标题/促销语',
  `category_id` bigint NOT NULL COMMENT '类目ID，关联 pms_product_category',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '主图，多张图片URL存JSON数组或逗号分隔',
  `main_video` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主图视频URL',
  `detail_pics` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详情图，多张图片URL存JSON数组或逗号分隔',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '展示价/起售价，单位元',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `lock_stock` int NOT NULL DEFAULT 0 COMMENT '锁定库存（已下单未支付）',
  `sale` int NOT NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-上架，0-下架，2-待上架',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序，升序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `limit_per_user` int NULL DEFAULT NULL COMMENT '每人限购数量，NULL 表示不限购',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort`(`sort` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品详情表（SPU）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_product
-- ----------------------------
INSERT INTO `pms_product` VALUES (1, '新鲜红富士苹果 5kg 礼盒装', '顺丰包邮', 2, 'https://picsum.photos/seed/apple/200/200', '', '/api/file/descs/e75bf30a600246a4a94e9a498f9acd6c.jpeg', 59.90, 0, 0, 18, 1, 0, '2026-03-14 21:49:38', '2026-03-23 21:14:30', NULL);
INSERT INTO `pms_product` VALUES (2, '特仑苏纯牛奶 250ml*12 提', '品质之选', 1, 'https://picsum.photos/seed/milk/200/200', NULL, NULL, 49.90, 453, 0, 3, 1, 1, '2026-03-14 21:49:38', '2026-03-29 21:41:33', NULL);
INSERT INTO `pms_product` VALUES (3, '测试', '顺丰包邮', 1, '/api/file/images/1b499978a79e458896d6769fa7ebd325.jpg,/api/file/images/5c8c88ec47da41b3b073f58578bc06d5.webp,/api/file/images/0959ee2baa6e40d2b17f0cf1acc65b34.jpg,/api/file/images/51b9c2a57f2f4c69b87f4dd88730c383.jpg', '/api/file/videos/858fb97b24464688a2cdfa2a12d2bd8b.mp4', '/api/file/descs/c2ae9d03a94c46269bf57e560e5b9b44.webp,/api/file/descs/7243305815be4a97b8f608d5082c0fd9.webp,/api/file/descs/00566d68ff5a4385b7487b30e4a52351.webp', 150.00, 0, 0, 1, 0, 0, '2026-03-15 11:54:51', '2026-03-26 21:04:45', NULL);

-- ----------------------------
-- Table structure for pms_product_category
-- ----------------------------
DROP TABLE IF EXISTS `pms_product_category`;
CREATE TABLE `pms_product_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类目名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序，升序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sort`(`sort` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品类目表（一级）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_product_category
-- ----------------------------
INSERT INTO `pms_product_category` VALUES (1, '水果蔬菜', 0, 1, '2026-03-14 21:24:52');
INSERT INTO `pms_product_category` VALUES (2, '米面粮油', 2, 1, '2026-03-14 21:59:31');
INSERT INTO `pms_product_category` VALUES (3, '测试类目', 0, 1, '2026-03-15 16:56:25');

-- ----------------------------
-- Table structure for pms_product_sku
-- ----------------------------
DROP TABLE IF EXISTS `pms_product_sku`;
CREATE TABLE `pms_product_sku`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` bigint NOT NULL COMMENT '商品ID，关联 pms_product',
  `spec_values` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '规格值，如：颜色:红,尺码:L',
  `sku_pic` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SKU 主图（规格图）',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'SKU 售价，单位元',
  `stock` int NOT NULL DEFAULT 0 COMMENT 'SKU 库存',
  `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商家编码/SKU编码',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '上下架：1-上架，0-下架',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sku_code`(`sku_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品SKU表（规格）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_product_sku
-- ----------------------------
INSERT INTO `pms_product_sku` VALUES (1, 1, '{\"规格名\":\"规格值\",\"颜色\":\"红\"}', 'https://picsum.photos/seed/apple/200/200', 59.90, 156, 'SKU-APPLE-001', 1, 0, '2026-03-14 21:58:50', '2026-03-15 09:54:06');
INSERT INTO `pms_product_sku` VALUES (2, 2, '{\"规格名\":\"规格值\"}', 'https://picsum.photos/seed/milk/200/200', 49.90, 85, 'SKU-MILK-001', 1, 0, '2026-03-14 21:58:50', '2026-03-15 10:02:09');

-- ----------------------------
-- Table structure for ums_admin
-- ----------------------------
DROP TABLE IF EXISTS `ums_admin`;
CREATE TABLE `ums_admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名，全局唯一',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'BCrypt 加密密码，禁止明文存储',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '显示昵称',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '账号状态：1-启用，0-禁用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '账号创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_admin
-- ----------------------------
INSERT INTO `ums_admin` VALUES (1, 'admin', '$2a$10$TWQm1YIUgTxGkt.7.td1TuU.9Cl5Hqw6CJ1eAitmI4zqYnNBNRJhy', '超级管理员', '', 1, '2026-03-11 21:33:28');
INSERT INTO `ums_admin` VALUES (2, 'haitong', '$2a$10$AdvxdqtvBPmxiQsP6zVMnO3yiTQ8l/AgpC0kVmjBRIiWBQl89rBOG', '杂货店仝老板', 'haitongt6@gmail.com', 1, '2026-03-14 08:00:41');

-- ----------------------------
-- Table structure for ums_admin_role_relation
-- ----------------------------
DROP TABLE IF EXISTS `ums_admin_role_relation`;
CREATE TABLE `ums_admin_role_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `admin_id` bigint NOT NULL COMMENT '管理员 ID',
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_admin_role_relation
-- ----------------------------
INSERT INTO `ums_admin_role_relation` VALUES (20, 1, 1);
INSERT INTO `ums_admin_role_relation` VALUES (35, 2, 6);
INSERT INTO `ums_admin_role_relation` VALUES (36, 2, 4);
INSERT INTO `ums_admin_role_relation` VALUES (37, 2, 5);

-- ----------------------------
-- Table structure for ums_member
-- ----------------------------
DROP TABLE IF EXISTS `ums_member`;
CREATE TABLE `ums_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号（唯一登录凭证）',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` int NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'C端会员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member
-- ----------------------------
INSERT INTO `ums_member` VALUES (1, '13127108261', '用户8261', NULL, 1, '2026-03-16 21:15:43', '2026-03-16 21:15:43');

-- ----------------------------
-- Table structure for ums_permission
-- ----------------------------
DROP TABLE IF EXISTS `ums_permission`;
CREATE TABLE `ums_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pid` bigint NOT NULL DEFAULT 0 COMMENT '父权限 ID，0 表示根节点',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称（界面展示用）',
  `value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识符，如 pms:product:read',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '节点类型：0-目录，1-菜单，2-按钮',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 57 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_permission
-- ----------------------------
INSERT INTO `ums_permission` VALUES (1, 0, '仪表盘', '/', 'LayoutDashboard', 1);
INSERT INTO `ums_permission` VALUES (2, 0, '商品管理', '', 'Package', 0);
INSERT INTO `ums_permission` VALUES (3, 0, '订单管理', '/orders', 'ClipboardList', 1);
INSERT INTO `ums_permission` VALUES (25, 0, '系统设置', '', 'Settings', 0);
INSERT INTO `ums_permission` VALUES (26, 25, '角色管理', '/settings/roles', 'Shield', 1);
INSERT INTO `ums_permission` VALUES (27, 25, '用户管理', '/settings/users', 'Users', 1);
INSERT INTO `ums_permission` VALUES (28, 26, '角色列表', 'ums:role:list', NULL, 2);
INSERT INTO `ums_permission` VALUES (29, 26, '新增角色', 'ums:role:add', NULL, 2);
INSERT INTO `ums_permission` VALUES (30, 26, '编辑角色', 'ums:role:edit', NULL, 2);
INSERT INTO `ums_permission` VALUES (31, 26, '删除角色', 'ums:role:delete', NULL, 2);
INSERT INTO `ums_permission` VALUES (32, 25, '菜单管理', '/settings/menus', 'List', 1);
INSERT INTO `ums_permission` VALUES (33, 32, '菜单列表', 'ums:menu:list', NULL, 2);
INSERT INTO `ums_permission` VALUES (34, 32, '新增按钮', 'ums:button:add', NULL, 2);
INSERT INTO `ums_permission` VALUES (35, 32, '编辑按钮', 'ums:button:edit', NULL, 2);
INSERT INTO `ums_permission` VALUES (36, 32, '删除按钮', 'ums:button:delete', NULL, 2);
INSERT INTO `ums_permission` VALUES (37, 25, '添加管理员', 'ums:admin:add', NULL, 2);
INSERT INTO `ums_permission` VALUES (38, 25, '管理员列表', 'ums:admin:list', NULL, 2);
INSERT INTO `ums_permission` VALUES (39, 25, '编辑管理员', 'ums:admin:edit', NULL, 2);
INSERT INTO `ums_permission` VALUES (40, 25, '删除管理员', 'ums:admin:delete', NULL, 2);
INSERT INTO `ums_permission` VALUES (44, 2, '类目管理', '/products/categories', 'FolderOpen', 1);
INSERT INTO `ums_permission` VALUES (45, 44, '类目列表', 'pms:category:list', NULL, 2);
INSERT INTO `ums_permission` VALUES (46, 44, '新增类目', 'pms:category:add', NULL, 2);
INSERT INTO `ums_permission` VALUES (47, 44, '编辑类目', 'pms:category:edit', NULL, 2);
INSERT INTO `ums_permission` VALUES (48, 44, '删除类目', 'pms:category:delete', NULL, 2);
INSERT INTO `ums_permission` VALUES (49, 2, '商品列表', '/products', NULL, 1);
INSERT INTO `ums_permission` VALUES (50, 49, '新增商品', 'pms:product:add', NULL, 2);
INSERT INTO `ums_permission` VALUES (51, 49, '编辑商品', 'pms:product:edit', NULL, 2);
INSERT INTO `ums_permission` VALUES (52, 49, '商品列表', 'pms:product:list', NULL, 2);
INSERT INTO `ums_permission` VALUES (53, 0, '订单列表', 'oms:order:list', NULL, 2);
INSERT INTO `ums_permission` VALUES (54, 0, '订单详情', 'oms:order:detail', NULL, 2);
INSERT INTO `ums_permission` VALUES (55, 0, '关闭订单', 'oms:order:close', NULL, 2);
INSERT INTO `ums_permission` VALUES (56, 0, '模拟支付成功', 'oms:pay:mock:success', NULL, 2);

-- ----------------------------
-- Table structure for ums_role
-- ----------------------------
DROP TABLE IF EXISTS `ums_role`;
CREATE TABLE `ums_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称，如 SUPER_ADMIN',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_role
-- ----------------------------
INSERT INTO `ums_role` VALUES (1, 'SUPER_ADMIN', '超级管理员，拥有所有权限', 1);
INSERT INTO `ums_role` VALUES (4, '财务', '对账、查看订单', 1);
INSERT INTO `ums_role` VALUES (5, '团长', '查看订单、查看仪表盘', 1);
INSERT INTO `ums_role` VALUES (6, '查看角色', '', 1);

-- ----------------------------
-- Table structure for ums_role_permission_relation
-- ----------------------------
DROP TABLE IF EXISTS `ums_role_permission_relation`;
CREATE TABLE `ums_role_permission_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  `permission_id` bigint NOT NULL COMMENT '权限 ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 140 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ums_role_permission_relation
-- ----------------------------
INSERT INTO `ums_role_permission_relation` VALUES (17, 1, 1);
INSERT INTO `ums_role_permission_relation` VALUES (18, 1, 2);
INSERT INTO `ums_role_permission_relation` VALUES (19, 1, 3);
INSERT INTO `ums_role_permission_relation` VALUES (20, 1, 5);
INSERT INTO `ums_role_permission_relation` VALUES (46, 1, 26);
INSERT INTO `ums_role_permission_relation` VALUES (47, 1, 25);
INSERT INTO `ums_role_permission_relation` VALUES (51, 1, 27);
INSERT INTO `ums_role_permission_relation` VALUES (55, 1, 28);
INSERT INTO `ums_role_permission_relation` VALUES (56, 1, 29);
INSERT INTO `ums_role_permission_relation` VALUES (57, 1, 30);
INSERT INTO `ums_role_permission_relation` VALUES (58, 1, 31);
INSERT INTO `ums_role_permission_relation` VALUES (62, 1, 32);
INSERT INTO `ums_role_permission_relation` VALUES (63, 1, 33);
INSERT INTO `ums_role_permission_relation` VALUES (64, 1, 34);
INSERT INTO `ums_role_permission_relation` VALUES (65, 1, 35);
INSERT INTO `ums_role_permission_relation` VALUES (66, 1, 36);
INSERT INTO `ums_role_permission_relation` VALUES (70, 4, 3);
INSERT INTO `ums_role_permission_relation` VALUES (71, 5, 1);
INSERT INTO `ums_role_permission_relation` VALUES (72, 5, 5);
INSERT INTO `ums_role_permission_relation` VALUES (82, 1, 42);
INSERT INTO `ums_role_permission_relation` VALUES (83, 1, 43);
INSERT INTO `ums_role_permission_relation` VALUES (84, 1, 44);
INSERT INTO `ums_role_permission_relation` VALUES (86, 1, 45);
INSERT INTO `ums_role_permission_relation` VALUES (87, 1, 46);
INSERT INTO `ums_role_permission_relation` VALUES (88, 1, 47);
INSERT INTO `ums_role_permission_relation` VALUES (89, 1, 48);
INSERT INTO `ums_role_permission_relation` VALUES (93, 1, 49);
INSERT INTO `ums_role_permission_relation` VALUES (94, 1, 50);
INSERT INTO `ums_role_permission_relation` VALUES (95, 1, 51);
INSERT INTO `ums_role_permission_relation` VALUES (126, 1, 52);
INSERT INTO `ums_role_permission_relation` VALUES (127, 6, 2);
INSERT INTO `ums_role_permission_relation` VALUES (128, 6, 44);
INSERT INTO `ums_role_permission_relation` VALUES (129, 6, 45);
INSERT INTO `ums_role_permission_relation` VALUES (130, 6, 49);
INSERT INTO `ums_role_permission_relation` VALUES (131, 6, 51);
INSERT INTO `ums_role_permission_relation` VALUES (132, 6, 52);
INSERT INTO `ums_role_permission_relation` VALUES (133, 6, 25);
INSERT INTO `ums_role_permission_relation` VALUES (134, 6, 26);
INSERT INTO `ums_role_permission_relation` VALUES (135, 6, 28);
INSERT INTO `ums_role_permission_relation` VALUES (136, 1, 53);
INSERT INTO `ums_role_permission_relation` VALUES (137, 1, 54);
INSERT INTO `ums_role_permission_relation` VALUES (138, 1, 55);
INSERT INTO `ums_role_permission_relation` VALUES (139, 1, 56);

SET FOREIGN_KEY_CHECKS = 1;
