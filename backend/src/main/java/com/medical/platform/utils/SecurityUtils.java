package com.medical.platform.utils;

import com.medical.platform.exception.BusinessException;
import com.medical.platform.dto.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 
 * @author 成员B
 */
public class SecurityUtils {
    
    /**
     * 获取当前认证信息
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "未登录");
        }
        return authentication.getName();
    }
    
    /**
     * 获取当前用户 ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "未登录");
        }
        Object details = authentication.getDetails();
        if (details instanceof Long) {
            return (Long) details;
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "无法获取用户ID");
    }
    
    /**
     * 获取当前用户角色
     */
    public static String getCurrentRole() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "未登录");
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse(null);
    }
    
    /**
     * 判断当前用户是否为指定角色
     */
    public static boolean hasRole(String role) {
        String currentRole = getCurrentRole();
        return role.equals(currentRole);
    }
    
    /**
     * 判断是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}