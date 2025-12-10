-- 药品表
CREATE TABLE IF NOT EXISTS medicine (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '药品名称',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    unit VARCHAR(20) NOT NULL COMMENT '单位（盒/瓶/片等）',
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

-- 病例表
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

-- 处方表
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

-- 处方明细表
CREATE TABLE IF NOT EXISTS prescription_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL COMMENT '处方ID',
    medicine_id BIGINT NOT NULL COMMENT '药品ID',
    quantity INT NOT NULL COMMENT '数量',
    price DECIMAL(10,2) NOT NULL COMMENT '单价（快照）',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_prescription_id (prescription_id),
    INDEX idx_medicine_id (medicine_id)
) COMMENT '处方明细表';

-- 扩展payment表，添加订单类型字段
-- 注意：MySQL不支持IF NOT EXISTS，需要先检查列是否存在
-- 如果列已存在，这些语句会报错，可以忽略

-- 检查并添加order_type列
SET @dbname = DATABASE();
SET @tablename = 'payment';
SET @columnname = 'order_type';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' TINYINT DEFAULT 1 COMMENT ''订单类型 1-挂号费 2-药品费 3-合并订单'';')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加prescription_id列
SET @columnname = 'prescription_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT COMMENT ''处方ID（如果是药品订单）'';')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加索引（如果不存在）
-- 注意：如果索引已存在会报错，可以忽略
SET @indexname = 'idx_order_type';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (index_name = @indexname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX ', @indexname, ' (order_type);')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @indexname = 'idx_prescription_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (index_name = @indexname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX ', @indexname, ' (prescription_id);')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

