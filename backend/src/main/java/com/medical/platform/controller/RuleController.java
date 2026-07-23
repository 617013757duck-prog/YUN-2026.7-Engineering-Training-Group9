package com.medical.platform.controller;

import com.medical.platform.dto.Result;
import com.medical.platform.entity.SafetyAlert;
import com.medical.platform.service.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 规则引擎控制器
 * 提供红旗症状检查、禁忌条件检查、安全警报管理等API接口
 */
@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@Tag(name = "规则引擎", description = "红旗症状检查、禁忌条件检查、安全警报管理")
public class RuleController {

    private final RuleService ruleService;

    /**
     * 对就诊记录执行安全检查
     */
    @PostMapping("/safety-check/{visitId}")
    @Operation(summary = "执行安全检查", description = "对就诊记录执行红旗症状检查和禁忌条件检查")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN')")
    public Result<RuleService.SafetyCheckResult> performSafetyCheck(@PathVariable Long visitId) {
        RuleService.SafetyCheckResult result = ruleService.performSafetyCheck(visitId);
        return Result.success("安全检查完成", result);
    }

    /**
     * 获取就诊记录的安全警报列表
     */
    @GetMapping("/alerts/{visitId}")
    @Operation(summary = "获取安全警报列表", description = "获取指定就诊记录的所有安全警报")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN', 'FOLLOWUP_STAFF')")
    public Result<List<SafetyAlert>> getSafetyAlerts(@PathVariable Long visitId) {
        List<SafetyAlert> alerts = ruleService.getSafetyAlerts(visitId);
        return Result.success(alerts);
    }

    /**
     * 处理安全警报
     */
    @PutMapping("/alerts/{alertId}/handle")
    @Operation(summary = "处理安全警报", description = "标记安全警报为已处理")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN')")
    public Result<SafetyAlert> handleSafetyAlert(
            @PathVariable Long alertId,
            @RequestParam String handledBy,
            @RequestParam String handleResult) {
        SafetyAlert alert = ruleService.handleSafetyAlert(alertId, handledBy, handleResult);
        if (alert == null) {
            return Result.success("安全警报不存在");
        }
        return Result.success("安全警报已处理", alert);
    }
}