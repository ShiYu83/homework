package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.PatientRegisterDTO;
import com.hospital.entity.Patient;
import com.hospital.entity.SysUser;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.SysUserRepository;
import com.hospital.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.repository.AppointmentRepository;
import com.hospital.entity.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {
    
    private final PatientRepository patientRepository;
    private final SysUserRepository sysUserRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public Patient register(PatientRegisterDTO dto) {
        // 检查身份证号是否已存在
        LambdaQueryWrapper<Patient> queryWrapper = new LambdaQueryWrapper<Patient>()
                .eq(Patient::getIdCard, dto.getIdCard());
        Patient existing = patientRepository.selectOne(queryWrapper);
        
        if (existing != null) {
            throw new RuntimeException("该身份证号已注册");
        }
        
        // 检查手机号是否已注册用户
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, dto.getPhone());
        SysUser existingUser = sysUserRepository.selectOne(userWrapper);
        if (existingUser != null) {
            throw new RuntimeException("该手机号已注册");
        }
        
        // 创建患者信息
        Patient patient = new Patient();
        patient.setName(dto.getName());
        patient.setIdCard(dto.getIdCard());
        patient.setPhone(dto.getPhone());
        patient.setGender(dto.getGender());
        
        if (dto.getBirthday() != null) {
            patient.setBirthday(LocalDate.parse(dto.getBirthday()));
        }
        
        patient.setAllergyHistory(dto.getAllergyHistory());
        patient.setCreditScore(100); // 默认诚信度100分
        patient.setCreateTime(LocalDateTime.now());
        
        patientRepository.insert(patient);
        
        // 创建系统用户账号
        SysUser sysUser = new SysUser();
        sysUser.setUsername(dto.getPhone()); // 使用手机号作为用户名
        sysUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        sysUser.setPhone(dto.getPhone());
        sysUser.setUserType(1); // 患者
        sysUser.setPatientId(patient.getId());
        sysUser.setStatus(1);
        sysUser.setCreateTime(LocalDateTime.now());
        
        sysUserRepository.insert(sysUser);
        
        return patient;
    }
    
    @Override
    public Patient getById(Long id) {
        return patientRepository.selectById(id);
    }
    
    @Override
    @Transactional
    public void updateInfo(Patient patient) {
        patient.setUpdateTime(LocalDateTime.now());
        patientRepository.updateById(patient);
    }
    
    @Override
    public List<Patient> getFamilyMembers(Long userId) {
        // TODO: 实现家庭成员查询逻辑
        return List.of();
    }
    
    @Override
    public Map<String, Object> getCreditInfo(Long patientId) {
        Patient patient = patientRepository.selectById(patientId);
        if (patient == null) {
            throw new RuntimeException("患者不存在");
        }
        
        // 查询预约记录统计
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getPatientId, patientId);
        List<Appointment> appointments = appointmentRepository.selectList(wrapper);
        
        int totalAppointments = appointments.size();
        long cancelledCount = appointments.stream()
                .filter(a -> a.getStatus() == 3)
                .count();
        long completedCount = appointments.stream()
                .filter(a -> a.getStatus() == 4)
                .count();
        
        // 计算诚信度（取消率影响）
        int creditScore = patient.getCreditScore() != null ? patient.getCreditScore() : 100;
        if (totalAppointments > 0) {
            double cancelRate = (double) cancelledCount / totalAppointments;
            // 取消率超过30%会降低诚信度
            if (cancelRate > 0.3) {
                creditScore = Math.max(0, creditScore - (int)(cancelRate * 20));
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("creditScore", creditScore);
        result.put("totalAppointments", totalAppointments);
        result.put("cancelledCount", cancelledCount);
        result.put("completedCount", completedCount);
        result.put("cancelRate", totalAppointments > 0 ? 
                String.format("%.2f%%", (double) cancelledCount / totalAppointments * 100) : "0%");
        result.put("level", getCreditLevel(creditScore));
        
        return result;
    }
    
    private String getCreditLevel(int score) {
        if (score >= 90) return "优秀";
        if (score >= 80) return "良好";
        if (score >= 70) return "一般";
        if (score >= 60) return "较差";
        return "差";
    }
}

