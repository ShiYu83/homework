package com.hospital.exception;

import com.hospital.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Object>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常：", e);
        // 开发环境返回详细错误信息，生产环境返回通用错误信息
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = "运行时异常：" + e.getClass().getSimpleName();
        }
        return ResponseEntity.ok(Result.error(message));
    }
    
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Result<Object>> handleDataAccessException(DataAccessException e) {
        log.error("数据库访问异常：", e);
        String message = "数据库操作失败";
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            message += "：" + e.getMessage();
        } else if (e.getCause() != null && e.getCause().getMessage() != null) {
            message += "：" + e.getCause().getMessage();
        } else {
            message += "，请检查数据库连接和表结构是否正确";
        }
        // 开发环境返回更详细的错误信息
        if (e.getCause() != null) {
            log.error("根本原因：", e.getCause());
        }
        return ResponseEntity.ok(Result.error(message));
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Object>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("处理器未找到：{}", e.getRequestURL());
        // 如果是访问 /api 路径，返回友好的提示信息
        String requestPath = e.getRequestURL();
        if (requestPath != null && requestPath.contains("/api") && !requestPath.contains("/api/")) {
            return ResponseEntity.ok(Result.success("这是后端API服务器。请访问前端应用: http://localhost:3000", null));
        }
        String message = "资源未找到：" + requestPath;
        if (requestPath != null && requestPath.contains("/api")) {
            message += "。请检查API路径是否正确，或访问前端应用: http://localhost:3000";
        }
        return ResponseEntity.ok(Result.error(message));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception e) {
        log.error("系统异常：{} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        
        // 特殊处理 NoResourceFoundException（Spring Boot 3.x）
        String exceptionName = e.getClass().getSimpleName();
        if (exceptionName.contains("NoResourceFoundException") || 
            (e.getMessage() != null && e.getMessage().contains("No static resource"))) {
            String requestPath = "";
            try {
                // 尝试从异常消息中提取路径
                String msg = e.getMessage();
                if (msg != null && msg.contains("No static resource")) {
                    // 提取路径信息
                    if (msg.contains("/api")) {
                        return ResponseEntity.ok(Result.success("这是后端API服务器。请访问前端应用: http://localhost:3000", null));
                    }
                }
            } catch (Exception ex) {
                // 忽略
            }
            return ResponseEntity.ok(Result.success("这是后端API服务器。请访问前端应用: http://localhost:3000", null));
        }
        
        // 开发环境返回详细错误信息
        String message = "系统错误：" + exceptionName;
        if (e.getMessage() != null) {
            message += " - " + e.getMessage();
        }
        return ResponseEntity.ok(Result.error(message));
    }
}


