package com.medical.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.platform.dto.Result;
import com.medical.platform.entity.Citation;
import com.medical.platform.service.CitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 引用记录控制器
 * 提供引用记录的查询API接口
 *
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/citations")
@RequiredArgsConstructor
@Tag(name = "引用记录管理", description = "AI分析引用记录的查询")
public class CitationController {

    private final CitationService citationService;

    @GetMapping
    @Operation(summary = "分页查询引用记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_STAFF', 'FOLLOWUP_STAFF')")
    public Result<Page<Citation>> getCitations(
            @Parameter(description = "就诊ID") @RequestParam(required = false) Long visitId,
            @Parameter(description = "AI运行记录ID") @RequestParam(required = false) Long agentRunId,
            @Parameter(description = "指南ID") @RequestParam(required = false) Long guidelineId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Citation> page = new Page<>(pageNum, pageSize);
        Page<Citation> result = citationService.getCitations(visitId, agentRunId, guidelineId, page);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询引用记录详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICAL_STAFF', 'FOLLOWUP_STAFF')")
    public Result<Citation> getCitationById(
            @Parameter(description = "引用记录ID") @PathVariable Long id) {
        Citation citation = citationService.getCitationById(id);
        if (citation == null) {
            return Result.error("引用记录不存在");
        }
        return Result.success(citation);
    }
}