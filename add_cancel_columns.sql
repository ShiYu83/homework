-- 添加预约取消相关字段
ALTER TABLE appointment 
ADD COLUMN cancel_time DATETIME COMMENT '取消时间',
ADD COLUMN cancel_reason VARCHAR(500) COMMENT '取消原因';
