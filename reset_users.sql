DELETE FROM sys_user WHERE username IN ('admin', 'doctor001');
INSERT INTO sys_user (username, password, user_type, status, phone) 
VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e', 3, 1, '13800138000'),
('doctor001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e', 2, 1, '13900139000');
SELECT * FROM sys_user WHERE username IN ('admin', 'doctor001');