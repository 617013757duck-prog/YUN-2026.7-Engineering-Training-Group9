package com.medical.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent运行记录实体
 * 用于记录AI Agent的执行历史和版本信息
 *
 * @author 贺孟缘
 */
@Data
@TableName("agent_runs")
public class AgentRun {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 就诊ID
     */
    private Long visitId;

    /**
     * Agent类型：RISK_CHECK、SAFETY_CHECK、AI_ANALYZE等
     */
    private String agentType;

    /**
     * 模型版本
     */
    private String modelVersion;

    /**
     * Prompt版本
     */
    private String promptVersion;

    /**
     * 知识库版本
     */
    private String knowledgeBaseVersion;

    /**
     * 输入数据（JSON格式）
     */
    private String inputData;

    /**
     * 输出数据（JSON格式）
     */
    private String outputData;

    /**
     * 执行时间（毫秒）
     */
    private Integer executionTimeMs;

    /**
     * 执行状态：SUCCESS、FAILED
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 触发用户ID
     */
    private Long triggeredBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}