package com.hospital.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptTester {
    public static void main(String[] args) {
        // 测试密码
        String rawPassword = "123456";
        
        // 创建编码器
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成新的哈希值
        String newHash = encoder.encode(rawPassword);
        System.out.println("新生成的BCrypt哈希值: " + newHash);
        System.out.println("哈希值长度: " + newHash.length());
        
        // 测试我们使用的哈希值
        String ourHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pQ0e";
        boolean matches = encoder.matches(rawPassword, ourHash);
        System.out.println("我们的哈希值是否匹配: " + matches);
    }
}