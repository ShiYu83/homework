@echo off
chcp 65001 >nul
echo ========================================
echo 快速修复管理员账户
echo ========================================
echo.

echo 正在连接数据库并创建管理员账户...
echo.

docker exec -i hospital-mysql mysql -uhospital_user -phospital123 hospital <<EOF
INSERT INTO sys_user (username, password, phone, user_type, status, create_time) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e', '13800000000', 3, 1, NOW())
ON DUPLICATE KEY UPDATE 
    password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e',
    user_type = 3,
    status = 1;

SELECT id, username, user_type, status FROM sys_user WHERE username = 'admin';
EOF

echo.
echo ========================================
echo 修复完成！
echo ========================================
echo.
echo 管理员账户信息：
echo   用户名: admin
echo   密码: 123456
echo.
echo 现在可以在前端登录了：http://localhost:3000/login
echo.
pause





