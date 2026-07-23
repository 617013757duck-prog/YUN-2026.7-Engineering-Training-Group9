package com.medical.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 安全警报实体类
 */
@Data
@TableName("safety_alerts")
public class SafetyAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long visitId;

    private String alertType;

    private String alertLevel;

    private String alertContent;

    private String triggerSource;

    private Integer status;

    private String handledBy;

    private String handleResult;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}