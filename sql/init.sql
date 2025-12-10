-- 创建数据库
CREATE DATABASE IF NOT EXISTS hospital DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hospital;

-- 患者表
CREATE TABLE patient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    id_card VARCHAR(18) NOT NULL UNIQUE COMMENT '身份证号',
    phone VARCHAR(11) NOT NULL COMMENT '手机号',
    gender TINYINT NOT NULL COMMENT '性别 0-女 1-男',
    birthday DATE COMMENT '出生日期',
    allergy_history TEXT COMMENT '过敏史',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone),
    INDEX idx_id_card (id_card)
) COMMENT '患者表';

-- 科室表
CREATE TABLE department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '科室名称',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '科室编码',
    description TEXT COMMENT '科室描述',
    status TINYINT DEFAULT 1 COMMENT '状态 0-停用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '科室表';

-- 医生表
CREATE TABLE doctor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '医生姓名',
    department_id BIGINT NOT NULL COMMENT '科室ID',
    title VARCHAR(20) NOT NULL COMMENT '职称',
    specialty VARCHAR(100) COMMENT '专长',
    introduction TEXT COMMENT '简介',
    status TINYINT DEFAULT 1 COMMENT '状态 0-停用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_department_id (department_id)
) COMMENT '医生表';

-- 排班表
CREATE TABLE schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    department_id BIGINT NOT NULL COMMENT '科室ID',
    schedule_date DATE NOT NULL COMMENT '排班日期',
    time_slot VARCHAR(20) NOT NULL COMMENT '时间段',
    total_count INT NOT NULL DEFAULT 0 COMMENT '总号源数',
    reserved_count INT NOT NULL DEFAULT 0 COMMENT '已预约数',
    status TINYINT DEFAULT 1 COMMENT '状态 0-停诊 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_doctor_date (doctor_id, schedule_date),
    INDEX idx_department_date (department_id, schedule_date)
) COMMENT '排班表';

-- 预约记录表
CREATE TABLE appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    schedule_id BIGINT NOT NULL COMMENT '排班ID',
    appointment_no VARCHAR(20) NOT NULL UNIQUE COMMENT '预约号',
    appointment_date DATE NOT NULL COMMENT '预约日期',
    time_slot VARCHAR(20) NOT NULL COMMENT '时间段',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-待支付 2-已预约 3-已取消 4-已完成',
    fee DECIMAL(10,2) NOT NULL COMMENT '挂号费',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient_id (patient_id),
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_create_time (create_time)
) COMMENT '预约记录表';

-- 用户表（用于登录认证）
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    phone VARCHAR(11) NOT NULL COMMENT '手机号',
    user_type TINYINT NOT NULL COMMENT '用户类型 1-患者 2-医生 3-管理员',
    patient_id BIGINT COMMENT '患者ID',
    doctor_id BIGINT COMMENT '医生ID',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) COMMENT '系统用户表';

-- 插入测试数据
INSERT INTO department (name, code, description) VALUES
('内科', 'NEIKE', '内科疾病诊疗'),
('外科', 'WAIKE', '外科手术诊疗'),
('儿科', 'ERKE', '儿童疾病诊疗'),
('妇产科', 'FUCHANKE', '妇科产科诊疗');

INSERT INTO doctor (name, department_id, title, specialty, introduction) VALUES
('张医生', 1, '主任医师', '心血管疾病', '从事心血管疾病诊疗30年'),
('李医生', 1, '副主任医师', '消化系统疾病', '擅长消化系统疾病诊治'),
('王医生', 2, '主任医师', '骨科手术', '骨科手术专家'),
('赵医生', 3, '副主任医师', '儿童常见病', '儿科疾病诊疗专家');

INSERT INTO sys_user (username, password, phone, user_type, patient_id) VALUES
('patient001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e', '13800138000', 1, NULL);

