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
 * 登录控制器（兼容前端接口）
 * 
 * 前端调用：POST /api/login
 * 该控制器是为了兼容前端接口路径
 * 
 * @author 贺孟缘
 */
@Tag(name = "登录接口", description = "前端兼容的登录接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {
    
    private final UserService userService;
    
    @Operation(summary = "用户登录", description = "用户登录接口，返回 JWT Token（前端兼容路径）")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }
}