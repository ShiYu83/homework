package com.hospital.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 连接字符集初始化器
 * 在应用启动后初始化所有连接池连接的字符集
 */
@Slf4j
@Component
public class ConnectionCharsetInitializer implements ApplicationListener<ContextRefreshedEvent> {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("初始化连接池字符集...");
        // 预热连接池，确保所有连接都设置了正确的字符集
        for (int i = 0; i < 5; i++) {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci");
                stmt.execute("SET CHARACTER SET utf8mb4");
                stmt.execute("SET character_set_client = utf8mb4");
                stmt.execute("SET character_set_connection = utf8mb4");
                stmt.execute("SET character_set_results = utf8mb4");
            } catch (Exception e) {
                log.warn("初始化连接字符集失败: {}", e.getMessage());
            }
        }
        log.info("连接池字符集初始化完成");
    }
}

