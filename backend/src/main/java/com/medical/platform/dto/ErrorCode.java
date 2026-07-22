package com.medical.platform.dto;

import lombok.Getter;

/**
 * 错误码枚举
 * 
 * @author 成员B
 */
@Getter
public enum ErrorCode {
    
    // 通用错误码
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统错误"),
    
    // 用户相关错误码
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    USERNAME_EXISTS(1003, "用户名已存在"),
    PASSWORD_ERROR(1004, "密码错误"),
    
    // 就诊相关错误码
    VISIT_NOT_FOUND(2001, "就诊记录不存在"),
    VISIT_STATUS_ERROR(2002, "就诊状态错误"),
    VISIT_ALREADY_SUBMITTED(2003, "就诊信息已提交"),
    
    // 随访相关错误码
    FOLLOWUP_NOT_FOUND(3001, "随访计划不存在"),
    FOLLOWUP_STATUS_ERROR(3002, "随访状态错误"),
    
    // AI 相关错误码
    AI_SERVICE_ERROR(4001, "AI 服务异常"),
    AI_TIMEOUT(4002, "AI 服务超时");
    
    private final Integer code;
    private final String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}