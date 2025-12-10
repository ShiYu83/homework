package com.hospital.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * 数据源配置和连接测试
 */
@Slf4j
@Component
@Order(1)
public class DataSourceConfig implements CommandLineRunner {
    
    private final DataSource dataSource;
    
    public DataSourceConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("开始测试数据库连接...");
        try (Connection connection = dataSource.getConnection();
             java.sql.Statement stmt = connection.createStatement()) {
            
            // 强制设置连接的字符集（确保所有字符集变量都设置）
            stmt.execute("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci");
            stmt.execute("SET CHARACTER SET utf8mb4");
            stmt.execute("SET character_set_client = utf8mb4");
            stmt.execute("SET character_set_connection = utf8mb4");
            stmt.execute("SET character_set_results = utf8mb4");
            
            // 验证字符集设置
            try (java.sql.ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE 'character_set%'")) {
                log.info("当前连接的字符集设置:");
                while (rs.next()) {
                    log.info("  {} = {}", rs.getString(1), rs.getString(2));
                }
            }
            
            DatabaseMetaData metaData = connection.getMetaData();
            log.info("数据库连接成功！");
            log.info("数据库产品名称: {}", metaData.getDatabaseProductName());
            log.info("数据库产品版本: {}", metaData.getDatabaseProductVersion());
            log.info("驱动名称: {}", metaData.getDriverName());
            log.info("驱动版本: {}", metaData.getDriverVersion());
            log.info("数据库URL: {}", metaData.getURL());
            log.info("用户名: {}", metaData.getUserName());
            
            // 测试查询中文字符
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT name FROM department LIMIT 1")) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    log.info("测试查询中文: {}", name);
                    // 验证是否正确
                    if (name.contains("?")) {
                        log.error("警告：查询结果包含乱码字符！");
                    }
                }
            }
        } catch (Exception e) {
            log.error("数据库连接失败！", e);
            throw new RuntimeException("数据库连接失败: " + e.getMessage(), e);
        }
    }
}

