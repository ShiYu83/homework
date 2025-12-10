-- Fix charset encoding issue
-- Execute this script to fix the character encoding in database

USE hospital;

-- Set charset for this session
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Clear and re-insert department data with correct encoding
DELETE FROM department;
INSERT INTO department (name, code, description, status) VALUES
('内科', 'NEIKE', '内科疾病诊疗', 1),
('外科', 'WAIKE', '外科手术诊疗', 1),
('儿科', 'ERKE', '儿童疾病诊疗', 1),
('妇产科', 'FUCHANKE', '妇科产科诊疗', 1);

-- Clear and re-insert doctor data with correct encoding
DELETE FROM doctor;
INSERT INTO doctor (name, department_id, title, specialty, introduction, status) VALUES
('张医生', 1, '主任医师', '心血管疾病', '从事心血管疾病诊疗30年', 1),
('李医生', 1, '副主任医师', '消化系统疾病', '擅长消化系统疾病诊治', 1),
('王医生', 2, '主任医师', '骨科手术', '骨科手术专家', 1),
('赵医生', 3, '副主任医师', '儿童常见病', '儿科疾病诊疗专家', 1);

-- Verify the fix
SELECT id, name, code, description FROM department;
SELECT id, name, department_id, title, specialty FROM doctor;






