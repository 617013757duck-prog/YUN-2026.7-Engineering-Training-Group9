package com.medical.platform.controller;

import com.medical.platform.dto.LoginRequest;
import com.medical.platform.dto.LoginResponse;
import com.medical.platform.dto.Result;
import com.medical.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * @author 成员B
 */
@Tag(name = "认证管理", description = "用户登录、注销等接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @Operation(summary = "用户登录", description = "用户登录接口，返回 JWT Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }
    
    @Operation(summary = "用户注销", description = "用户注销接口")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT 无状态，客户端删除 Token 即可
        return Result.success("注销成功");
    }
}