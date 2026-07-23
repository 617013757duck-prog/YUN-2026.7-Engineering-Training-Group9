package com.medical.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * AI分析请求DTO
 * 
 * 对应AI服务的AnalyzeRequest
 */
@Data
public class AIAnalyzeRequest {

    private Long visitId;

    private List<String> symptoms;

    private Integer patientAge;

    private Integer durationDays;

    private String additionalInfo;
}