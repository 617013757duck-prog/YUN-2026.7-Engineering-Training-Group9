package com.medical.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 随访计划实体类
 */
@Data
@TableName("followup_plans")
public class FollowupPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String planCode;

    private Long patientId;

    private Long visitId;

    private String planName;

    private String planType; // CHRONIC_DISEASE, POST_OPERATION, HEALTH_MONITORING

    private LocalDate startDate;

    private LocalDate endDate;

    private String frequency; // DAILY, WEEKLY, MONTHLY

    private String status; // ACTIVE, PAUSED, COMPLETED, CANCELLED

    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}