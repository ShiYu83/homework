package com.hospital.util;

import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 字符编码修复工具
 * 在应用启动时检查并修复数据库中的字符编码问题
 */
@Component
@Order(2) // 在DataSourceConfig之后执行
public class CharsetFixer implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(CharsetFixer.class);
    
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    
    public CharsetFixer(DepartmentRepository departmentRepository, DoctorRepository doctorRepository) {
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
    }
    
    @Override
    public void run(String... args) {
        log.info("开始检查并修复字符编码...");
        
        try {
            // 检查第一个部门的数据
            List<Department> departments = departmentRepository.selectList(null);
            if (!departments.isEmpty()) {
                String name = departments.get(0).getName();
                log.info("当前部门名称: {}", name);
                
                // 如果包含乱码字符，说明数据有问题，需要修复
                if (name != null && (name.contains("?") || name.contains("鍐") || name.contains("å") || name.contains("澶"))) {
                    log.warn("检测到字符编码问题，正在修复数据...");
                    fixData();
                } else {
                    log.info("字符编码检查通过，数据正常");
                }
            } else {
                log.info("部门表为空，跳过字符编码检查");
            }
            
        } catch (Exception e) {
            log.error("字符编码检查和修复失败: {}", e.getMessage(), e);
        }
    }
    
    private void fixData() {
        try {
            log.info("开始修复部门数据...");
            
            // 删除旧数据（先删除依赖数据，避免外键约束）
            List<Doctor> allDoctors = doctorRepository.selectList(null);
            for (Doctor doctor : allDoctors) {
                doctorRepository.deleteById(doctor.getId());
            }
            List<Department> allDepts = departmentRepository.selectList(null);
            for (Department dept : allDepts) {
                departmentRepository.deleteById(dept.getId());
            }
            
            log.info("已删除 {} 条医生记录和 {} 条部门记录", allDoctors.size(), allDepts.size());
            
            // 重新插入正确的数据（使用UTF-8字符串）
            Department dept1 = new Department();
            dept1.setName("内科");
            dept1.setCode("NEIKE");
            dept1.setDescription("内科疾病诊疗");
            dept1.setStatus(1);
            departmentRepository.insert(dept1);
            
            Department dept2 = new Department();
            dept2.setName("外科");
            dept2.setCode("WAIKE");
            dept2.setDescription("外科手术诊疗");
            dept2.setStatus(1);
            departmentRepository.insert(dept2);
            
            Department dept3 = new Department();
            dept3.setName("儿科");
            dept3.setCode("ERKE");
            dept3.setDescription("儿童疾病诊疗");
            dept3.setStatus(1);
            departmentRepository.insert(dept3);
            
            Department dept4 = new Department();
            dept4.setName("妇产科");
            dept4.setCode("FUCHANKE");
            dept4.setDescription("妇科产科诊疗");
            dept4.setStatus(1);
            departmentRepository.insert(dept4);
            
            log.info("开始修复医生数据...");
            
            Doctor doctor1 = new Doctor();
            doctor1.setName("张医生");
            doctor1.setDepartmentId(1L);
            doctor1.setTitle("主任医师");
            doctor1.setSpecialty("心血管疾病");
            doctor1.setIntroduction("从事心血管疾病诊疗30年");
            doctor1.setStatus(1);
            doctorRepository.insert(doctor1);
            
            Doctor doctor2 = new Doctor();
            doctor2.setName("李医生");
            doctor2.setDepartmentId(1L);
            doctor2.setTitle("副主任医师");
            doctor2.setSpecialty("消化系统疾病");
            doctor2.setIntroduction("擅长消化系统疾病诊治");
            doctor2.setStatus(1);
            doctorRepository.insert(doctor2);
            
            Doctor doctor3 = new Doctor();
            doctor3.setName("王医生");
            doctor3.setDepartmentId(2L);
            doctor3.setTitle("主任医师");
            doctor3.setSpecialty("骨科手术");
            doctor3.setIntroduction("骨科手术专家");
            doctor3.setStatus(1);
            doctorRepository.insert(doctor3);
            
            Doctor doctor4 = new Doctor();
            doctor4.setName("赵医生");
            doctor4.setDepartmentId(3L);
            doctor4.setTitle("副主任医师");
            doctor4.setSpecialty("儿童常见病");
            doctor4.setIntroduction("儿科疾病诊疗专家");
            doctor4.setStatus(1);
            doctorRepository.insert(doctor4);
            
            log.info("数据修复完成！");
            
            // 验证修复结果
            List<Department> fixedDepts = departmentRepository.selectList(null);
            if (!fixedDepts.isEmpty()) {
                log.info("修复后的部门名称: {}", fixedDepts.get(0).getName());
            }
            
        } catch (Exception e) {
            log.error("修复数据时出错: {}", e.getMessage(), e);
        }
    }
}

