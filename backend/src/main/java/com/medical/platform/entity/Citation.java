package com.medical.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 引用记录实体类
 */
@Data
@TableName("citations")
public class Citation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long visitId;

    private Long agentRunId;

    private Long guidelineId;

    private String citationText;

    private String sourceSection;

    private BigDecimal relevanceScore;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}