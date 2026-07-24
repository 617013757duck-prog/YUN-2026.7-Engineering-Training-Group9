package com.medical.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * AI分析请求DTO
 *
 * 对应AI服务的AnalyzeRequest
 * 使用@JsonProperty注解确保与Python API的蛇形命名一致
 */
@Data
public class AIAnalyzeRequest {

    @JsonProperty("visit_id")
    private Long visitId;

    private List<String> symptoms;

    @JsonProperty("patient_age")
    private Integer patientAge;

    @JsonProperty("duration_days")
    private Integer durationDays;

    @JsonProperty("additional_info")
    private String additionalInfo;
}