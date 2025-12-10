USE hospital;

CREATE TABLE IF NOT EXISTS medicine (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '药品名称',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    unit VARCHAR(20) NOT NULL COMMENT '单位',
    specification VARCHAR(100) COMMENT '规格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '上架状态 0-下架 1-上架',
    category VARCHAR(50) COMMENT '药品分类',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_category (category)
) COMMENT '药品表';

CREATE TABLE IF NOT EXISTS medical_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL COMMENT '预约ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    diagnosis TEXT COMMENT '诊断结果',
    symptoms TEXT COMMENT '症状描述',
    advice TEXT COMMENT '医嘱建议',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_appointment_id (appointment_id),
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id)
) COMMENT '病例表';

CREATE TABLE IF NOT EXISTS prescription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medical_record_id BIGINT NOT NULL COMMENT '病例ID',
    appointment_id BIGINT NOT NULL COMMENT '预约ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '处方总金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-待支付 2-已支付',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_medical_record_id (medical_record_id),
    INDEX idx_appointment_id (appointment_id),
    INDEX idx_status (status)
) COMMENT '处方表';

CREATE TABLE IF NOT EXISTS prescription_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL COMMENT '处方ID',
    medicine_id BIGINT NOT NULL COMMENT '药品ID',
    quantity INT NOT NULL COMMENT '数量',
    price DECIMAL(10,2) NOT NULL COMMENT '单价快照',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_prescription_id (prescription_id),
    INDEX idx_medicine_id (medicine_id)
) COMMENT '处方明细表';

