package com.hospital.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 当前用户工具类
 * 用于获取当前登录用户的信息
 */
public class CurrentUserUtil {
    
    private CurrentUserUtil() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 获取当前用户ID
     * @return 当前用户ID
     */
    public static Long getCurrentUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return (Long) request.getAttribute("userId");
    }
    
    /**
     * 获取当前用户类型
     * @return 当前用户类型（1-患者，2-医生，3-管理员）
     */
    public static Integer getCurrentUserType() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return (Integer) request.getAttribute("userType");
    }
    
    /**
     * 获取当前用户名
     * @return 当前用户名
     */
    public static String getCurrentUsername() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return (String) request.getAttribute("username");
    }
    
    /**
     * 获取当前HttpServletRequest
     * @return 当前HttpServletRequest
     */
    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }
}