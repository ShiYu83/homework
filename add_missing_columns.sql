-- 为appointment表添加缺失的payment_time和complete_time字段
ALTER TABLE appointment ADD COLUMN payment_time DATETIME NULL COMMENT '支付时间';
ALTER TABLE appointment ADD COLUMN complete_time DATETIME NULL COMMENT '完成时间';
