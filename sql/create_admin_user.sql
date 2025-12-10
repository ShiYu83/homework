-- 创建管理员账户脚本
-- 如果管理员账户不存在，则创建；如果存在，则更新密码

USE hospital;

-- 检查并创建管理员账户
INSERT INTO sys_user (username, password, phone, user_type, status, create_time) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e', '13800000000', 3, 1, NOW())
ON DUPLICATE KEY UPDATE 
    password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e',
    user_type = 3,
    status = 1;

-- 验证管理员账户
SELECT id, username, user_type, status, phone FROM sys_user WHERE username = 'admin';





