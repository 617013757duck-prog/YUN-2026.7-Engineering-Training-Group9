package com.medical.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("triage_results")
public class TriageResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long visitId;

    private String riskLevel;

    private String riskScore;

    private String redFlagSymptoms;

    private String contraindications;

    private String recommendations;

    private String aiModelVersion;

    private String promptVersion;

    private String knowledgeBaseVersion;

    private String citations;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}