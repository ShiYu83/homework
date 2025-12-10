package com.hospital.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 响应编码拦截器
 * 确保所有响应都使用UTF-8编码
 */
@Component
public class ResponseEncodingInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 设置请求编码
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (Exception e) {
            // 忽略异常
        }
        
        // 设置响应编码
        response.setCharacterEncoding("UTF-8");
        
        // 如果响应头中没有Content-Type，则设置默认值
        String contentType = response.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            response.setContentType("application/json;charset=UTF-8");
        } else if (!contentType.contains("charset")) {
            // 如果Content-Type存在但没有charset，添加charset
            response.setContentType(contentType + "; charset=UTF-8");
        }
        
        return true;
    }
}

