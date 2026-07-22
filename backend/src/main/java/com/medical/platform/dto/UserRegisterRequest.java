package com.medical.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户注册请求 DTO
 * 
 * @author 成员B
 */
@Data
public class UserRegisterRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名必须是4-20位字母、数字或下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_!@#$%^&*()]{6,20}$", message = "密码必须是6-20位字母、数字或特殊字符")
    private String password;
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(PATIENT|MEDICAL_STAFF|FOLLOWUP_STAFF|ADMIN)$", message = "角色不正确")
    private String role;
}