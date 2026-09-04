CREATE DATABASE IF NOT EXISTS `sky_take_out`;

USE `sky_take_out`;

DROP TABLE IF EXISTS `employee`;

CREATE TABLE `employee` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(32)  NOT NULL COMMENT '姓名',
    `username`    VARCHAR(32)  NOT NULL COMMENT '用户名（登录账号）',
    `password`    VARCHAR(64)  NOT NULL COMMENT '密码',
    `phone`       VARCHAR(11)  NOT NULL COMMENT '手机号',
    `sex`         VARCHAR(2)   NOT NULL COMMENT '性别 1:男，2:女',
    `id_number`   VARCHAR(18)  NOT NULL COMMENT '身份证号',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0:禁用，1:启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT       DEFAULT NULL COMMENT '创建人ID',
    `update_user` BIGINT       DEFAULT NULL COMMENT '修改人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC
  COMMENT = '员工表';

INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`,
                        `create_time`, `update_time`, `create_user`, `update_user`)
VALUES (1, '管理员', 'admin', '123456', '13812312312', '1', '110101199001010047', 1,
        '2022-02-15 15:51:20', '2022-02-17 09:16:20', 10, 1);
