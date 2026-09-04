CREATE DATABASE IF NOT EXISTS `sky_take_out`;

USE sky_take_out;

DROP TABLE IF EXISTS `employee`;

-- CREATE TABLE `employee` (
--     `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
--     `name` VARCHAR(50) NOT NULL COMMENT '姓名',
--     `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
--     `password` VARCHAR(64) NOT NULL COMMENT '密码',
--     `phone` VARCHAR(11) NOT NULL COMMENT '手机号',
--     `sex` VARCHAR(2) NOT NULL COMMENT '性别',
--     `id_number` VARCHAR(18) NOT NULL COMMENT '身份证号',
--     `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0:禁用,1:正常',
--     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
--     `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
--     `create_user` BIGINT NOT NULL COMMENT '创建人',
--     `update_user` BIGINT NOT NULL COMMENT '修改人'
-- ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工表';

CREATE TABLE `employee` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '姓名',
    `username` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '用户名',
    `password` varchar(64) COLLATE utf8_bin NOT NULL COMMENT '密码',
    `phone` varchar(11) COLLATE utf8_bin NOT NULL COMMENT '手机号',
    `sex` varchar(2) COLLATE utf8_bin NOT NULL COMMENT '性别',
    `id_number` varchar(18) COLLATE utf8_bin NOT NULL COMMENT '身份证号',
    `status` int NOT NULL DEFAULT '1' COMMENT '状态 0:禁用，1:启用',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `create_user` bigint DEFAULT NULL COMMENT '创建人',
    `update_user` bigint DEFAULT NULL COMMENT '修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_username` (`username`)
) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8mb3 COLLATE = utf8_bin COMMENT = '员工信息';

INSERT INTO
    `employee`
VALUES (
        1,
        '管理员',
        'admin',
        '123456',
        '13812312312',
        '1',
        '110101199001010047',
        1,
        '2022-02-15 15:51:20',
        '2022-02-17 09:16:20',
        10,
        1
    );