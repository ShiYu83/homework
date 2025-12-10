-- 数据库表结构更新脚本
-- 用于添加新功能和权限管理

USE hospital;

-- 添加患者诚信度字段
ALTER TABLE patient 
ADD COLUMN credit_score INT DEFAULT 100 COMMENT '诚信度分数 0-100，默认100';

-- 创建诊室表
CREATE TABLE IF NOT EXISTS consulting_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_no VARCHAR(20) NOT NULL UNIQUE COMMENT '诊室编号',
    name VARCHAR(50) NOT NULL COMMENT '诊室名称',
    department_id BIGINT NOT NULL COMMENT '所属科室ID',
    doctor_id BIGINT COMMENT '所属医生ID',
    location VARCHAR(100) COMMENT '位置',
    status TINYINT DEFAULT 1 COMMENT '状态 0-停用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_department_id (department_id),
    INDEX idx_doctor_id (doctor_id)
) COMMENT '诊室表';

-- 创建调班申请表
CREATE TABLE IF NOT EXISTS schedule_adjustment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    schedule_id BIGINT NOT NULL COMMENT '排班ID',
    original_date DATE NOT NULL COMMENT '原排班日期',
    original_time_slot VARCHAR(20) NOT NULL COMMENT '原时间段',
    new_date DATE COMMENT '新排班日期',
    new_time_slot VARCHAR(20) COMMENT '新时间段',
    reason VARCHAR(500) COMMENT '调班原因',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待审核 1-已通过 2-已拒绝',
    reviewer_id BIGINT COMMENT '审核人ID（管理员）',
    review_comment VARCHAR(500) COMMENT '审核意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_status (status)
) COMMENT '调班申请表';

-- 插入管理员测试账号
INSERT INTO sys_user (username, password, phone, user_type, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e', '13800000000', 3, 1)
ON DUPLICATE KEY UPDATE username=username;

-- 插入医生测试账号（关联医生ID）
INSERT INTO sys_user (username, password, phone, user_type, doctor_id, status) VALUES
('doctor001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e', '13800138001', 2, 1, 1),
('doctor002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e', '13800138002', 2, 2, 1)
ON DUPLICATE KEY UPDATE username=username;

-- 更新现有患者默认诚信度
UPDATE patient SET credit_score = 100 WHERE credit_score IS NULL;

