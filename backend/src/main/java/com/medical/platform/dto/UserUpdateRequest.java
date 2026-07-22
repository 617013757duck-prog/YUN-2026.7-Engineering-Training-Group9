package com.medical.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户信息修改请求 DTO
 * 
 * @author 成员B
 */
@Data
public class UserUpdateRequest {
    
    private String realName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String role;
    
    private Integer status;
}