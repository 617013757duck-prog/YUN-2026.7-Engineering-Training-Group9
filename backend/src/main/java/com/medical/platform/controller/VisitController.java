package com.medical.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.platform.dto.Result;
import com.medical.platform.dto.VisitCreateRequest;
import com.medical.platform.dto.VisitVO;
import com.medical.platform.entity.Visit;
import com.medical.platform.entity.VisitStatus;
import com.medical.platform.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
@Tag(name = "就诊管理", description = "就诊记录的CRUD和状态管理")
public class VisitController {

    private final VisitService visitService;

    @PostMapping
    @Operation(summary = "创建就诊记录")
    public Result<Visit> createVisit(@RequestBody VisitCreateRequest request) {
        Visit visit = visitService.createVisit(request);
        return Result.success("就诊记录创建成功", visit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取就诊详情")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN', 'FOLLOWUP_STAFF')")
    public Result<VisitVO> getVisit(@PathVariable Long id) {
        VisitVO vo = visitService.getVisitById(id);
        return Result.success(vo);
    }

    @GetMapping
    @Operation(summary = "分页查询就诊列表")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN', 'FOLLOWUP_STAFF')")
    public Result<Page<VisitVO>> listVisits(
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "风险等级") @RequestParam(required = false) String riskLevel,
            @Parameter(description = "患者ID") @RequestParam(required = false) Long patientId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Visit> page = new Page<>(pageNum, pageSize);
        Page<VisitVO> result = visitService.listVisits(status, riskLevel, patientId, page);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新就诊记录")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN')")
    public Result<Visit> updateVisit(@PathVariable Long id, @RequestBody VisitCreateRequest request) {
        Visit visit = visitService.updateVisit(id, request);
        return Result.success("就诊记录更新成功", visit);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新就诊状态")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN')")
    public Result<Visit> updateStatus(@PathVariable Long id, @RequestParam String status) {
        VisitStatus visitStatus = VisitStatus.valueOf(status);
        Visit visit = visitService.updateStatus(id, visitStatus);
        return Result.success("状态更新成功", visit);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除就诊记录")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteVisit(@PathVariable Long id) {
        visitService.deleteVisit(id);
        return Result.success("就诊记录删除成功");
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "执行AI分析")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN')")
    public Result<Visit> performAIAnalysis(@PathVariable Long id) {
        Visit visit = visitService.performAIAnalysis(id);
        return Result.success("AI分析完成", visit);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交就诊记录进行AI分析")
    public Result<Visit> submitForAnalysis(@PathVariable Long id) {
        Visit visit = visitService.submitForAnalysis(id);
        return Result.success("已提交进行AI分析", visit);
    }
}