-- ============================================================
-- 测试账号批量插入 / 删除脚本
--
-- 约定：所有测试账号的 username 统一以 test_ 开头（test_001 ~ test_100），
--       删除时按该前缀清理即可，不影响真实账号。
--
-- 密码统一为 123456（MD5：e10adc3949ba59abbe56e057f20f883e），与 admin 初始密码一致。
-- ============================================================

-- 插入 100 个测试账号（MySQL 8+，使用递归 CTE 生成序号）
INSERT INTO employee (name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100
)
SELECT
    CONCAT('测试员工', LPAD(n, 3, '0'))                                       AS name,
    CONCAT('test_', LPAD(n, 3, '0'))                                          AS username,
    'e10adc3949ba59abbe56e057f20f883e'                                        AS password,
    CONCAT('138', LPAD(FLOOR(RAND() * 100000000), 8, '0'))                    AS phone,
    IF(n % 2 = 0, '1', '0')                                                   AS sex,
    CONCAT('11010119900101', LPAD(FLOOR(RAND() * 10000), 4, '0'))             AS id_number,
    1                                                                         AS status,
    NOW()                                                                     AS create_time,
    NOW()                                                                     AS update_time,
    1                                                                         AS create_user,
    1                                                                         AS update_user
FROM seq;

-- 删除全部测试账号（只删 test_ 前缀，不会误删真实账号）
-- DELETE FROM employee WHERE username LIKE 'test\_%';
