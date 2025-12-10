package com.hospital.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

/**
 * MyBatis 字符集拦截器
 * 在每次执行SQL前设置连接的字符集为utf8mb4
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class CharsetInterceptor implements Interceptor {
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Executor executor = (Executor) invocation.getTarget();
        
        // 获取当前连接并设置字符集
        try {
            // 通过反射获取transaction
            java.lang.reflect.Field transactionField = null;
            Class<?> executorClass = executor.getClass();
            
            // 查找transaction字段
            while (executorClass != null && executorClass != Object.class) {
                try {
                    transactionField = executorClass.getDeclaredField("transaction");
                    break;
                } catch (NoSuchFieldException e) {
                    executorClass = executorClass.getSuperclass();
                }
            }
            
            if (transactionField != null) {
                transactionField.setAccessible(true);
                org.apache.ibatis.transaction.Transaction transaction = 
                    (org.apache.ibatis.transaction.Transaction) transactionField.get(executor);
                
                if (transaction != null) {
                    Connection connection = transaction.getConnection();
                    if (connection != null && !connection.isClosed()) {
                        // 设置连接的字符集（强制设置所有相关变量）
                        try (Statement stmt = connection.createStatement()) {
                            stmt.execute("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci");
                            stmt.execute("SET CHARACTER SET utf8mb4");
                            stmt.execute("SET character_set_client = utf8mb4");
                            stmt.execute("SET character_set_connection = utf8mb4");
                            stmt.execute("SET character_set_results = utf8mb4");
                            log.debug("已设置连接字符集为 utf8mb4");
                        } catch (Exception e) {
                            log.warn("设置连接字符集失败: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 如果无法设置字符集，记录日志但继续执行
            log.warn("获取连接设置字符集时出错: {}", e.getMessage());
        }
        
        // 继续执行原始方法
        return invocation.proceed();
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        // 可以在这里设置属性
    }
}

