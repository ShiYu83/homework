package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Appointment;
import com.hospital.entity.Payment;
import com.hospital.entity.Prescription;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.PaymentRepository;
import com.hospital.repository.PrescriptionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    
    /**
     * 创建支付订单（挂号费）
     */
    @PostMapping("/create")
    public ResponseEntity<Result<Map<String, Object>>> createPayment(
            @RequestParam Long appointmentId, 
            @RequestParam BigDecimal amount) {
        Appointment appointment = appointmentRepository.selectById(appointmentId);
        if (appointment == null) {
            return ResponseEntity.ok(Result.error(404, "预约不存在"));
        }
        
        Payment payment = new Payment();
        payment.setAppointmentId(appointmentId);
        payment.setOrderNo("PAY" + System.currentTimeMillis());
        payment.setAmount(amount);
        payment.setStatus(1); // 待支付
        payment.setOrderType(1); // 挂号费
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        
        paymentRepository.insert(payment);
        
        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("id", payment.getId());
        paymentInfo.put("orderNo", payment.getOrderNo());
        paymentInfo.put("amount", payment.getAmount());
        paymentInfo.put("appointmentId", payment.getAppointmentId());
        paymentInfo.put("status", payment.getStatus());
        paymentInfo.put("orderType", payment.getOrderType());
        
        return ResponseEntity.ok(Result.success("订单创建成功", paymentInfo));
    }
    
    /**
     * 创建合并订单（挂号费+药品费）
     */
    @PostMapping("/create-combined")
    public ResponseEntity<Result<Map<String, Object>>> createCombinedOrder(
            @RequestBody CreateCombinedOrderDTO dto) {
        Appointment appointment = appointmentRepository.selectById(dto.getAppointmentId());
        if (appointment == null) {
            return ResponseEntity.ok(Result.error(404, "预约不存在"));
        }
        
        BigDecimal totalAmount = appointment.getFee(); // 挂号费
        
        // 如果有处方，加上药品费用
        if (dto.getPrescriptionId() != null) {
            Prescription prescription = prescriptionRepository.selectById(dto.getPrescriptionId());
            if (prescription != null) {
                totalAmount = totalAmount.add(prescription.getTotalAmount());
            }
        }
        
        Payment payment = new Payment();
        payment.setAppointmentId(dto.getAppointmentId());
        payment.setPrescriptionId(dto.getPrescriptionId());
        payment.setOrderNo("PAY" + System.currentTimeMillis());
        payment.setAmount(totalAmount);
        payment.setStatus(1); // 待支付
        payment.setOrderType(3); // 合并订单
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        
        paymentRepository.insert(payment);
        
        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("id", payment.getId());
        paymentInfo.put("orderNo", payment.getOrderNo());
        paymentInfo.put("amount", payment.getAmount());
        paymentInfo.put("appointmentId", payment.getAppointmentId());
        paymentInfo.put("prescriptionId", payment.getPrescriptionId());
        paymentInfo.put("status", payment.getStatus());
        paymentInfo.put("orderType", payment.getOrderType());
        
        return ResponseEntity.ok(Result.success("合并订单创建成功", paymentInfo));
    }
    
    /**
     * 支付订单
     */
    @PostMapping("/pay")
    @Transactional
    public ResponseEntity<Result<Object>> pay(
            @RequestParam Long paymentId, 
            @RequestParam Integer paymentMethod) {
        Payment payment = paymentRepository.selectById(paymentId);
        if (payment == null) {
            return ResponseEntity.ok(Result.error(404, "订单不存在"));
        }
        
        if (payment.getStatus() != 1) {
            return ResponseEntity.ok(Result.error(400, "订单状态不正确"));
        }
        
        // 更新支付状态
        payment.setStatus(2); // 已支付
        payment.setPaymentMethod(paymentMethod);
        payment.setPayTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        paymentRepository.updateById(payment);
        
        // 更新预约状态
        Appointment appointment = appointmentRepository.selectById(payment.getAppointmentId());
        if (appointment != null) {
            // 支付成功后，无论什么类型的订单，都更新为已完成
            // 因为支付完成意味着整个就诊流程完成了
            appointment.setStatus(4); // 已完成
            appointment.setPaymentTime(LocalDateTime.now());
            // 如果还没有完成时间，设置完成时间
            if (appointment.getCompleteTime() == null) {
                appointment.setCompleteTime(LocalDateTime.now());
            }
            appointmentRepository.updateById(appointment);
        }
        
        // 如果有处方，更新处方状态
        if (payment.getPrescriptionId() != null) {
            Prescription prescription = prescriptionRepository.selectById(payment.getPrescriptionId());
            if (prescription != null && prescription.getStatus() == 1) {
                prescription.setStatus(2); // 已支付
                prescription.setUpdateTime(LocalDateTime.now());
                prescriptionRepository.updateById(prescription);
            }
        }
        
        return ResponseEntity.ok(Result.success("支付成功", null));
    }
    
    /**
     * 退款
     */
    @PostMapping("/refund")
    public ResponseEntity<Result<Object>> refund(
            @RequestParam Long paymentId, 
            @RequestParam(required = false) String reason) {
        Payment payment = paymentRepository.selectById(paymentId);
        if (payment == null) {
            return ResponseEntity.ok(Result.error(404, "订单不存在"));
        }
        
        if (payment.getStatus() != 2) {
            return ResponseEntity.ok(Result.error(400, "只有已支付的订单才能退款"));
        }
        
        payment.setStatus(3); // 已退款
        payment.setUpdateTime(LocalDateTime.now());
        paymentRepository.updateById(payment);
        
        return ResponseEntity.ok(Result.success("退款成功", null));
    }
    
    @Data
    public static class CreateCombinedOrderDTO {
        private Long appointmentId;
        private Long prescriptionId;
    }
}

