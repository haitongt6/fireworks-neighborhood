-- C 端会员表
CREATE TABLE `ums_member` (
  `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
  `phone`       varchar(20)  NOT NULL COMMENT '手机号（唯一登录凭证）',
  `nickname`    varchar(64)  DEFAULT NULL COMMENT '昵称',
  `avatar`      varchar(500) DEFAULT NULL COMMENT '头像URL',
  `status`      int(1)       NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端会员表';
