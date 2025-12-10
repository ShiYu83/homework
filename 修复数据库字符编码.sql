-- 修复数据库字符编码问题
-- 重新插入数据，确保使用正确的UTF-8编码

USE hospital;

-- 设置会话字符集
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修复部门数据
UPDATE department SET 
    name = '内科', 
    description = '内科疾病诊疗' 
WHERE id = 1;

UPDATE department SET 
    name = '外科', 
    description = '外科手术诊疗' 
WHERE id = 2;

UPDATE department SET 
    name = '儿科', 
    description = '儿童疾病诊疗' 
WHERE id = 3;

UPDATE department SET 
    name = '妇产科', 
    description = '妇科产科诊疗' 
WHERE id = 4;

-- 修复医生数据
UPDATE doctor SET 
    name = '张医生',
    title = '主任医师',
    specialty = '心血管疾病',
    introduction = '从事心血管疾病诊疗30年'
WHERE id = 1;

UPDATE doctor SET 
    name = '李医生',
    title = '副主任医师',
    specialty = '消化系统疾病',
    introduction = '擅长消化系统疾病诊治'
WHERE id = 2;

UPDATE doctor SET 
    name = '王医生',
    title = '主任医师',
    specialty = '骨科手术',
    introduction = '骨科手术专家'
WHERE id = 3;

UPDATE doctor SET 
    name = '赵医生',
    title = '副主任医师',
    specialty = '儿童常见病',
    introduction = '儿科疾病诊疗专家'
WHERE id = 4;

-- 验证修复结果
SELECT id, name, description FROM department;
SELECT id, name, title, specialty FROM doctor;

