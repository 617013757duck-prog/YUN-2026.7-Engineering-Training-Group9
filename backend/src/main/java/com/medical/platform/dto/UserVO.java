package com.medical.platform.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户视图对象（不包含密码等敏感信息）
 * 
 * @author 成员B
 */
@Data
public class UserVO {
    
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}