package com.medical.platform.controller;

import com.medical.platform.dto.*;
import com.medical.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 
 * @author 成员B
 */
@Tag(name = "用户管理", description = "用户注册、查询、修改、删除等接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 获取当前登录用户信息（前端兼容接口）
     * 前端调用：GET /api/user/info
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<UserVO> getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "未登录");
        }
        
        // 从认证信息中获取用户ID
        String username = authentication.getName();
        UserVO userVO = userService.getUserVOByUsername(username);
        return Result.success(userVO);
    }
    
    @Operation(summary = "用户注册", description = "注册新用户")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserRegisterRequest request) {
        UserVO vo = userService.register(request);
        return Result.success("注册成功", vo);
    }
    
    @Operation(summary = "分页查询用户", description = "分页查询用户列表，可按角色和关键字筛选")
    @GetMapping
    public Result<PageResult<UserVO>> getUserPage(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "角色") @RequestParam(required = false) String role,
            @Parameter(description = "关键字（用户名/姓名/手机号）") @RequestParam(required = false) String keyword) {
        PageResult<UserVO> page = userService.getUserPage(current, size, role, keyword);
        return Result.success(page);
    }
    
    @Operation(summary = "获取用户详情", description = "根据 ID 获取用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        UserVO vo = userService.getUserVOById(id);
        return Result.success(vo);
    }
    
    @Operation(summary = "更新用户信息", description = "更新用户基本信息")
    @PutMapping("/{id}")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserVO vo = userService.updateUser(id, request);
        return Result.success("更新成功", vo);
    }
    
    @Operation(summary = "删除用户", description = "逻辑删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }
}