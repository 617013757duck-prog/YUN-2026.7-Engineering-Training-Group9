package com.medical.platform.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 
 * @author 成员B
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private Integer code;
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}