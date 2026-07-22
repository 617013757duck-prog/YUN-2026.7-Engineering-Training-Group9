package com.medical.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("symptoms")
public class Symptom {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long visitId;

    private String symptomName;

    private String symptomCode;

    private String severity;

    private String onsetTime;

    private String duration;

    private String location;

    private String description;

    private String associatedSymptoms;

    private Integer isRedFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}