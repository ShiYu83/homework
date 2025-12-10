-- 添加医生头像字段
USE hospital;

-- 为doctor表添加avatar字段
ALTER TABLE doctor 
ADD COLUMN IF NOT EXISTS avatar VARCHAR(255) COMMENT '头像URL' AFTER consulting_room_id;


