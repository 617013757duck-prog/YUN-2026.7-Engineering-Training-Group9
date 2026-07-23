package com.medical.platform.controller;

import com.medical.platform.dto.FollowupPlanRequest;
import com.medical.platform.dto.FollowupTaskCompleteRequest;
import com.medical.platform.dto.Result;
import com.medical.platform.entity.FollowupPlan;
import com.medical.platform.entity.FollowupTask;
import com.medical.platform.service.FollowupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 随访管理控制器
 */
@RestController
@RequestMapping("/api/followup")
@RequiredArgsConstructor
@Tag(name = "随访管理", description = "随访计划和任务的CRUD管理")
public class FollowupController {

    private final FollowupService followupService;

    // ==================== 随访计划管理 ====================

    /**
     * 创建随访计划
     */
    @PostMapping("/plans")
    @Operation(summary = "创建随访计划")
    @PreAuthorize("hasAnyRole('FOLLOWUP_STAFF', 'ADMIN', 'MEDICAL_STAFF')")
    public Result<FollowupPlan> createPlan(@RequestBody FollowupPlanRequest request) {
        FollowupPlan plan = convertToPlan(request);
        FollowupPlan created = followupService.createPlan(plan);
        return Result.success("随访计划创建成功", created);
    }

    /**
     * 更新随访计划
     */
    @PutMapping("/plans/{id}")
    @Operation(summary = "更新随访计划")
    @PreAuthorize("hasAnyRole('FOLLOWUP_STAFF', 'ADMIN')")
    public Result<FollowupPlan> updatePlan(@PathVariable Long id, @RequestBody FollowupPlanRequest request) {
        FollowupPlan plan = convertToPlan(request);
        FollowupPlan updated = followupService.updatePlan(id, plan);
        return Result.success("随访计划更新成功", updated);
    }

    /**
     * 获取患者随访计划列表
     */
    @GetMapping("/plans/patient/{patientId}")
    @Operation(summary = "获取患者随访计划列表")
    @PreAuthorize("hasAnyRole('FOLLOWUP_STAFF', 'ADMIN', 'MEDICAL_STAFF')")
    public Result<List<FollowupPlan>> getPatientPlans(@PathVariable Long patientId) {
        List<FollowupPlan> plans = followupService.getPatientPlans(patientId);
        return Result.success(plans);
    }

    /**
     * 获取随访计划详情
     */
    @GetMapping("/plans/{id}")
    @Operation(summary = "获取随访计划详情")
    @PreAuthorize("hasAnyRole('FOLLOWUP_STAFF', 'ADMIN', 'MEDICAL_STAFF')")
    public Result<FollowupPlan> getPlanById(@PathVariable Long id) {
        FollowupPlan plan = followupService.getPlanById(id);
        return Result.success(plan);
    }

    /**
     * 删除随访计划
     */
    @DeleteMapping("/plans/{id}")
    @Operation(summary = "删除随访计划")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deletePlan(@PathVariable Long id) {
        followupService.deletePlan(id);
        return Result.success("随访计划删除成功");
    }

    // ==================== 随访任务管理 ====================

    /**
     * 创建随访任务
     */
    @PostMapping("/tasks")
    @Operation(summary = "创建随访任务")
    @PreAuthorize("hasAnyRole('FOLLOWUP_STAFF', 'ADMIN')")
    public Result<FollowupTask> createTask(@RequestBody FollowupTask task) {
        FollowupTask created = followupService.createTask(task);
        return Result.success("随访任务创建成功", created);
    }

    /**
     * 完成随访任务
     */
    @PutMapping("/tasks/{id}/complete")
    @Operation(summary = "完成随访任务")
    @PreAuthorize("hasAnyRole('FOLLOWUP_STAFF', 'ADMIN')")
    public Result<FollowupTask> completeTask(@PathVariable Long id, @RequestBody FollowupTaskCompleteRequest request) {
        FollowupTask completed = followupService.completeTask(
            id, request.getExecutorId(), request.getResult(), request.getNotes());
        return Result.success("随访任务完成", completed);
    }

    /**
     * 获取随访计划的任务列表
     */
    @GetMapping("/tasks/plan/{planId}")
    @Operation(summary = "获取随访计划的任务列表")
    @PreAuthorize("hasAnyRole('FOLLOWUP_STAFF', 'ADMIN', 'MEDICAL_STAFF')")
    public Result<List<FollowupTask>> getPlanTasks(@PathVariable Long planId) {
        List<FollowupTask> tasks = followupService.getPlanTasks(planId);
        return Result.success(tasks);
    }

    /**
     * 获取今日随访任务
     */
    @GetMapping("/tasks/today")
    @Operation(summary = "获取今日随访任务")
    @PreAuthorize("hasAnyRole('FOLLOWUP_STAFF', 'ADMIN')")
    public Result<List<FollowupTask>> getTodayTasks() {
        List<FollowupTask> tasks = followupService.getTodayTasks();
        return Result.success(tasks);
    }

    /**
     * 获取待执行的随访任务
     */
    @GetMapping("/tasks/pending")
    @Operation(summary = "获取待执行的随访任务")
    @PreAuthorize("hasAnyRole('FOLLOWUP_STAFF', 'ADMIN')")
    public Result<List<FollowupTask>> getPendingTasks() {
        List<FollowupTask> tasks = followupService.getPendingTasks();
        return Result.success(tasks);
    }

    /**
     * 为计划自动生成随访任务
     */
    @PostMapping("/tasks/generate/{planId}")
    @Operation(summary = "自动生成随访任务")
    @PreAuthorize("hasAnyRole('FOLLOWUP_STAFF', 'ADMIN')")
    public Result<Void> generateTasks(@PathVariable Long planId) {
        followupService.generateTasksForPlan(planId);
        return Result.success("随访任务生成成功");
    }

    /**
     * 转换DTO到实体
     */
    private FollowupPlan convertToPlan(FollowupPlanRequest request) {
        FollowupPlan plan = new FollowupPlan();
        plan.setPatientId(request.getPatientId());
        plan.setVisitId(request.getVisitId());
        plan.setPlanName(request.getPlanName());
        plan.setPlanType(request.getPlanType());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setFrequency(request.getFrequency());
        plan.setCreatorId(request.getCreatorId());
        return plan;
    }
}