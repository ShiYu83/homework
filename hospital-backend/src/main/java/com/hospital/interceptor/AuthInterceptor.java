package com.hospital.interceptor;

import com.hospital.annotation.RequireRole;
import com.hospital.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 权限拦截器
 * 用于验证用户身份和角色权限
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 静态资源直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        
        // 如果没有权限注解，检查类上的注解
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        
        // 如果没有权限要求，直接放行
        if (requireRole == null) {
            return true;
        }
        
        // 获取Token
        String token = getTokenFromRequest(request);
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\"}");
            return false;
        }
        
        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return false;
        }
        
        // 获取用户角色
        Claims claims = jwtUtil.getClaimsFromToken(token);
        Integer userType = claims.get("userType", Integer.class);
        
        // 检查角色权限
        int[] allowedRoles = requireRole.value();
        if (allowedRoles.length > 0) {
            boolean hasPermission = Arrays.stream(allowedRoles)
                    .anyMatch(role -> role == userType);
            
            if (!hasPermission) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
                return false;
            }
        }
        
        // 将用户信息存储到request中，供后续使用
        request.setAttribute("userId", claims.get("userId", Long.class));
        request.setAttribute("userType", userType);
        request.setAttribute("username", claims.getSubject());
        
        return true;
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

