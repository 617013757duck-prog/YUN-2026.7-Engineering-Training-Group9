package com.medical.platform.controller;

import com.medical.platform.dto.Result;
import com.medical.platform.dto.UserVO;
import com.medical.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息控制器（兼容前端接口）
 * 
 * 前端调用：GET /api/user/info
 * 该控制器是为了兼容前端接口路径
 * 
 * @author 贺孟缘
 */
@Tag(name = "用户信息接口", description = "前端兼容的用户信息接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserInfoController {
    
    private final UserService userService;
    
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息（前端兼容路径）")
    @GetMapping("/info")
    public Result<UserVO> getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "未登录");
        }
        
        // 从认证信息中获取用户名
        String username = authentication.getName();
        UserVO userVO = userService.getUserVOByUsername(username);
        return Result.success(userVO);
    }
}