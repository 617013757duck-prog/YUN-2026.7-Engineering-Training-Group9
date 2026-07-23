package com.medical.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 医疗指南实体类
 */
@Data
@TableName("medical_guidelines")
public class MedicalGuideline {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String category;

    private String content;

    private String source;

    private String version;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}