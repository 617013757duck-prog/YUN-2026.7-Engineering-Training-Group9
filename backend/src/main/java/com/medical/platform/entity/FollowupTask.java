package com.medical.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 随访任务实体类
 */
@Data
@TableName("followup_tasks")
public class FollowupTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskCode;

    private Long planId;

    private LocalDate scheduledDate;

    private LocalDate actualDate;

    private String status; // PENDING, COMPLETED, OVERDUE, CANCELLED

    private Long executorId;

    private String taskContent;

    private String result;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}