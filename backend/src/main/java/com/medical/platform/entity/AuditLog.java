package com.medical.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审计日志实体
 * 用于记录系统关键操作的审计日志
 *
 * @author 贺孟缘
 */
@Data
@TableName("audit_logs")
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 事件类型：LOGIN, LOGOUT, AI_ANALYZE, VISIT_CREATE, VISIT_UPDATE等
     */
    private String eventType;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 关联就诊ID（可选）
     */
    private Long visitId;

    /**
     * 详细信息（JSON格式）
     */
    private String details;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}