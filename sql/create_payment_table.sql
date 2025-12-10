USE hospital;

CREATE TABLE IF NOT EXISTS payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL COMMENT '预约ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    payment_method TINYINT COMMENT '支付方式 1-微信 2-支付宝 3-医保 4-现金',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-待支付 2-已支付 3-已退款',
    pay_time DATETIME COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    order_type TINYINT DEFAULT 1 COMMENT '订单类型 1-挂号费 2-药品费 3-合并订单',
    prescription_id BIGINT COMMENT '处方ID',
    INDEX idx_appointment_id (appointment_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_order_type (order_type),
    INDEX idx_prescription_id (prescription_id)
) COMMENT '支付订单表';

