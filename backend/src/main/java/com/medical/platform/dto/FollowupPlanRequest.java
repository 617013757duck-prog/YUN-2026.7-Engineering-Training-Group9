package com.medical.platform.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 随访计划创建请求DTO
 */
@Data
public class FollowupPlanRequest {

    private Long patientId;

    private Long visitId;

    private String planName;

    private String planType; // CHRONIC_DISEASE, POST_OPERATION, HEALTH_MONITORING

    private LocalDate startDate;

    private LocalDate endDate;

    private String frequency; // DAILY, WEEKLY, MONTHLY

    private Long creatorId;
}