package com.hospital.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private UserInfo userInfo;
    
    // 手动添加getter和setter方法
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public UserInfo getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private Integer userType;
        private String phone;
        private Long patientId; // 患者ID（当userType=1时）
        private Long doctorId;  // 医生ID（当userType=2时）
        
        // 手动添加getter和setter方法
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public Integer getUserType() {
            return userType;
        }
        
        public void setUserType(Integer userType) {
            this.userType = userType;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public Long getPatientId() {
            return patientId;
        }
        
        public void setPatientId(Long patientId) {
            this.patientId = patientId;
        }
        
        public Long getDoctorId() {
            return doctorId;
        }
        
        public void setDoctorId(Long doctorId) {
            this.doctorId = doctorId;
        }
    }
}

