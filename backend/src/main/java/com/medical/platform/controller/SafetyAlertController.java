package com.medical.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.platform.dto.Result;
import com.medical.platform.entity.SafetyAlert;
import com.medical.platform.service.SafetyAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 安全警报控制器
 * 提供警报的查询、处理等API接口
 *
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/safety-alerts")
@RequiredArgsConstructor
@Tag(name = "安全警报管理", description = "安全警报的查询、处理和删除")
public class SafetyAlertController {

    private final SafetyAlertService safetyAlertService;

    @GetMapping
    @Operation(summary = "分页查询安全警报")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_STAFF', 'FOLLOWUP_STAFF')")
    public Result<Page<SafetyAlert>> getAlerts(
            @Parameter(description = "警报等级") @RequestParam(required = false) String alertLevel,
            @Parameter(description = "处理状态(0-未处理,1-已处理)") @RequestParam(required = false) Integer status,
            @Parameter(description = "就诊ID") @RequestParam(required = false) Long visitId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SafetyAlert> page = new Page<>(pageNum, pageSize);
        Page<SafetyAlert> result = safetyAlertService.getAlerts(alertLevel, status, visitId, page);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询警报详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_STAFF', 'FOLLOWUP_STAFF')")
    public Result<SafetyAlert> getAlertById(
            @Parameter(description = "警报ID") @PathVariable Long id) {
        SafetyAlert alert = safetyAlertService.getById(id);
        if (alert == null) {
            return Result.error("安全警报不存在");
        }
        return Result.success(alert);
    }

    @PutMapping("/{id}/handle")
    @Operation(summary = "处理安全警报")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_STAFF')")
    public Result<SafetyAlert> handleAlert(
            @Parameter(description = "警报ID") @PathVariable Long id,
            @Parameter(description = "处理人") @RequestParam String handler,
            @Parameter(description = "处理结果") @RequestParam String handleResult) {
        SafetyAlert alert = safetyAlertService.handleAlert(id, handler, handleResult);
        if (alert == null) {
            return Result.error("安全警报不存在");
        }
        return Result.success("警报处理成功", alert);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除安全警报")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteAlert(
            @Parameter(description = "警报ID") @PathVariable Long id) {
        safetyAlertService.deleteAlert(id);
        return Result.success("警报删除成功");
    }
}