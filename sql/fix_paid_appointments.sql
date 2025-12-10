-- 修复所有已支付但状态不是"已完成"的预约记录
-- 将已支付（有payment_time）但状态为2（已预约）的预约更新为4（已完成）

UPDATE appointment 
SET status = 4,
    complete_time = COALESCE(complete_time, payment_time)
WHERE status = 2 
  AND payment_time IS NOT NULL
  AND id IN (
    SELECT DISTINCT appointment_id 
    FROM payment 
    WHERE status = 2  -- 已支付
  );

