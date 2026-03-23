-- ============================================================
--  马面裙数智化设计与打版系统 -- 数据库 DDL
--  数据库: MySQL 8.0    字符集: utf8mb4
--  前缀规范: hb_ (汉服打版)
-- ============================================================

-- ----------------------------
-- 1. 款式表 hb_style
-- 记录每一款马面裙的设计信息，与 AI 生成效果图绑定
-- ----------------------------
CREATE TABLE `hb_style` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT       COMMENT '主键 ID',
    `style_no`      VARCHAR(32)  NOT NULL                      COMMENT '款式编号 (业务唯一键, 如 MM-20260001)',
    `style_name`    VARCHAR(128) NOT NULL                      COMMENT '款式名称',
    `prompt`        TEXT                                       COMMENT '用户输入的中文提示词（AI 绘图用）',
    `ai_image_url`  VARCHAR(512)                               COMMENT 'AI 生成效果图的存储地址（OSS/本地路径）',
    `ai_task_id`    VARCHAR(128)                               COMMENT 'AI 绘图任务 ID（SD task_id / 批次号）',
    `ai_status`     TINYINT      NOT NULL DEFAULT 0            COMMENT 'AI 任务状态: 0-待生成 1-生成中 2-完成 3-失败',
    `color_scheme`  VARCHAR(256)                               COMMENT '配色方案（JSON 数组，存储主色调 HEX）',
    `fabric_type`   VARCHAR(64)                                COMMENT '面料类型（如：缎面、织锦、棉麻）',
    `remark`        VARCHAR(512)                               COMMENT '备注',
    `creator_id`    BIGINT                                     COMMENT '创建人 ID',
    `status`        TINYINT      NOT NULL DEFAULT 1            COMMENT '状态: 0-禁用 1-启用',
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0            COMMENT '逻辑删除: 0-正常 1-已删',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_style_no` (`style_no`),
    KEY `idx_ai_status` (`ai_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '马面裙款式表';


-- ----------------------------
-- 2. 尺寸规格表 hb_size_spec
-- 记录每个款式对应的尺寸参数，一个款式可以有多个尺码规格
-- ----------------------------
CREATE TABLE `hb_size_spec` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT       COMMENT '主键 ID',
    `style_id`        BIGINT        NOT NULL                      COMMENT '关联款式 ID (hb_style.id)',
    `size_label`      VARCHAR(32)   NOT NULL                      COMMENT '尺码标签（如：S/M/L/XL 或 155/160/165）',
    `skirt_length`    DECIMAL(8, 2) NOT NULL                      COMMENT '裙长（单位：cm）',
    `waist_girth`     DECIMAL(8, 2) NOT NULL                      COMMENT '腰围（单位：cm）',
    `door_width`      DECIMAL(8, 2) NOT NULL                      COMMENT '裙门宽（马面门襟宽，单位：cm）',
    `pleat_num`       INT           NOT NULL                      COMMENT '裙身褶子数量（每侧），通常 8~16',
    `hip_girth`       DECIMAL(8, 2)                               COMMENT '臀围（参考值，单位：cm）',
    `hem_width`       DECIMAL(8, 2)                               COMMENT '下摆展开宽（计算值，单位：cm）',
    `seam_allowance`  DECIMAL(6, 2) NOT NULL DEFAULT 1.50         COMMENT '缝份（单位：cm，默认 1.5cm）',
    `remark`          VARCHAR(256)                                COMMENT '备注',
    `is_deleted`      TINYINT       NOT NULL DEFAULT 0            COMMENT '逻辑删除: 0-正常 1-已删',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_style_id` (`style_id`),
    CONSTRAINT `fk_size_spec_style` FOREIGN KEY (`style_id`) REFERENCES `hb_style` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '马面裙尺寸规格表';


-- ----------------------------
-- 3. 打版任务表 hb_pattern_task
-- 记录一次完整的打版任务：输入尺寸 -> 计算裁片 -> 输出 DXF/TIFF
-- ----------------------------
CREATE TABLE `hb_pattern_task` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT       COMMENT '主键 ID',
    `task_no`         VARCHAR(32)   NOT NULL                      COMMENT '任务编号 (业务唯一键, 如 PT-20260001)',
    `style_id`        BIGINT        NOT NULL                      COMMENT '关联款式 ID',
    `size_spec_id`    BIGINT        NOT NULL                      COMMENT '关联尺寸规格 ID',
    `skirt_length`    DECIMAL(8, 2) NOT NULL                      COMMENT '裙长快照（cm）',
    `waist_girth`     DECIMAL(8, 2) NOT NULL                      COMMENT '腰围快照（cm）',
    `door_width`      DECIMAL(8, 2) NOT NULL                      COMMENT '裙门宽快照（cm）',
    `pleat_num`       INT           NOT NULL                      COMMENT '褶子数量快照',
    `seam_allowance`  DECIMAL(6, 2) NOT NULL DEFAULT 1.50         COMMENT '缝份快照（cm）',
    `pieces_json`     LONGTEXT                                    COMMENT '裁片坐标数据（JSON，MamianEngine 输出结果）',
    `dxf_path`        VARCHAR(512)                                COMMENT '生成的 DXF 文件存储路径',
    `tiff_path`       VARCHAR(512)                                COMMENT '生成的 TIFF 印花底稿存储路径',
    `task_status`     TINYINT       NOT NULL DEFAULT 0            COMMENT '任务状态: 0-待执行 1-计算中 2-完成 3-失败',
    `error_msg`       VARCHAR(1024)                               COMMENT '失败原因',
    `operator_id`     BIGINT                                      COMMENT '操作人 ID',
    `is_deleted`      TINYINT       NOT NULL DEFAULT 0            COMMENT '逻辑删除: 0-正常 1-已删',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_no` (`task_no`),
    KEY `idx_style_id` (`style_id`),
    KEY `idx_size_spec_id` (`size_spec_id`),
    KEY `idx_task_status` (`task_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '马面裙打版任务表';
