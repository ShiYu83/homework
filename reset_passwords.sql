-- 删除现有用户
DELETE FROM sys_user WHERE username='admin' OR username='doctor001';

-- 使用简单的方法重新创建用户，稍后通过应用程序来正确加密密码
INSERT INTO sys_user(username, password, user_type, phone, status) VALUES('admin', 'temp_password_admin', 3, '13800138000', 1);
INSERT INTO sys_user(username, password, user_type, phone, status) VALUES('doctor001', 'temp_password_doctor', 2, '13900139000', 1);

-- 查看创建的用户
SELECT id, username, user_type, phone, status FROM sys_user WHERE username='admin' OR username='doctor001';