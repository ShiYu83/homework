DELETE FROM sys_user WHERE username='admin' OR username='doctor001';

INSERT INTO sys_user(username, password, user_type, phone, status) VALUES('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e', 3, '13800138000', 1);

INSERT INTO sys_user(username, password, user_type, phone, status) VALUES('doctor001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e', 2, '13900139000', 1);

SELECT id, username, user_type, phone, status, LENGTH(password) FROM sys_user WHERE username='admin' OR username='doctor001';