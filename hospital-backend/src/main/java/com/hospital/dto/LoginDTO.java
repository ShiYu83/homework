package com.hospital.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String username;
    private String password;
    
    // 手动添加getter和setter方法
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}

