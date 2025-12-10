-- 初始化管理员账户
-- 执行此脚本以确保管理员账户存在

USE hospital;

-- 删除可能存在的旧管理员账户（可选）
-- DELETE FROM sys_user WHERE username = 'admin';

-- 插入管理员账户（如果不存在则插入，存在则更新）
INSERT INTO sys_user (username, password, phone, user_type, status, create_time) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e', '13800000000', 3, 1, NOW())
ON DUPLICATE KEY UPDATE 
    password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e',
    user_type = 3,
    status = 1,
    phone = '13800000000';

-- 验证管理员账户
SELECT 
    id, 
    username, 
    user_type, 
    status, 
    phone,
    CASE user_type 
        WHEN 1 THEN '患者'
        WHEN 2 THEN '医生'
        WHEN 3 THEN '管理员'
        ELSE '未知'
    END AS user_type_name
FROM sys_user 
WHERE username = 'admin';

-- 显示所有用户（用于调试）
SELECT 
    id, 
    username, 
    user_type, 
    status, 
    phone
FROM sys_user
ORDER BY user_type, id;





