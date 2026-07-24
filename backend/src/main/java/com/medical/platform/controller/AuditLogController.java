package com.medical.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.platform.dto.Result;
import com.medical.platform.entity.AuditLog;
import com.medical.platform.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 审计日志控制器
 * 用于查询和管理审计日志
 *
 * @author 贺孟缘
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "审计日志管理", description = "审计日志的查询和管理")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "分页查询审计日志")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<AuditLog>> getAuditLogs(
            @Parameter(description = "事件类型") @RequestParam(required = false) String eventType,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "就诊ID") @RequestParam(required = false) Long visitId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {

        Page<AuditLog> page = new Page<>(pageNum, pageSize);
        Page<AuditLog> result = auditLogService.getAuditLogs(eventType, userId, visitId, page);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取审计日志详情")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AuditLog> getAuditLog(@PathVariable Long id) {
        AuditLog auditLog = auditLogService.getAuditLogById(id);
        if (auditLog == null) {
            return Result.error(404, "审计日志不存在");
        }
        return Result.success(auditLog);
    }
}