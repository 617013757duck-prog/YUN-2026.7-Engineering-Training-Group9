package com.medical.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.platform.dto.Result;
import com.medical.platform.entity.Symptom;
import com.medical.platform.service.SymptomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
@Tag(name = "症状管理", description = "症状记录的CRUD")
public class SymptomController {

    private final SymptomService symptomService;

    @GetMapping("/{id}")
    @Operation(summary = "获取症状详情")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN')")
    public Result<Symptom> getSymptom(@PathVariable Long id) {
        Symptom symptom = symptomService.getById(id);
        return Result.success(symptom);
    }

    @GetMapping("/visit/{visitId}")
    @Operation(summary = "获取就诊的症状列表")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN')")
    public Result<List<Symptom>> getSymptomsByVisit(@PathVariable Long visitId) {
        List<Symptom> symptoms = symptomService.getByVisitId(visitId);
        return Result.success(symptoms);
    }

    @GetMapping("/visit/{visitId}/red-flags")
    @Operation(summary = "获取就诊的红旗症状")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN')")
    public Result<List<Symptom>> getRedFlagSymptoms(@PathVariable Long visitId) {
        List<Symptom> symptoms = symptomService.getRedFlagSymptoms(visitId);
        return Result.success(symptoms);
    }

    @GetMapping
    @Operation(summary = "分页查询症状")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN')")
    public Result<Page<Symptom>> listSymptoms(
            @Parameter(description = "症状名称") @RequestParam(required = false) String symptomName,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Symptom> page = new Page<>(pageNum, pageSize);
        Page<Symptom> result = symptomService.listSymptoms(symptomName, page);
        return Result.success(result);
    }

    @PostMapping
    @Operation(summary = "创建症状")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN')")
    public Result<Symptom> createSymptom(@RequestBody Symptom symptom) {
        Symptom created = symptomService.create(symptom);
        return Result.success("症状创建成功", created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新症状")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF', 'ADMIN')")
    public Result<Symptom> updateSymptom(@PathVariable Long id, @RequestBody Symptom symptom) {
        Symptom updated = symptomService.update(id, symptom);
        return Result.success("症状更新成功", updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除症状")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteSymptom(@PathVariable Long id) {
        symptomService.delete(id);
        return Result.success("症状删除成功");
    }
}