package com.medical.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * AI分析响应DTO
 * 
 * 对应AI服务的AnalyzeResponse
 */
@Data
public class AIAnalyzeResponse {

    private Long visitId;

    private String riskLevel;

    private String analysis;

    private List<GuidelineRef> guidelineRefs;

    private List<String> suggestions;

    private String modelVersion;

    private String promptVersion;

    private String kbVersion;

    private String timestamp;

    @Data
    public static class GuidelineRef {
        private String title;
        private String section;
    }
}