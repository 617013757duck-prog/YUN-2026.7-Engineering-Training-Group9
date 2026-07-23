package com.medical.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * AI分析响应DTO
 *
 * 对应AI服务的AnalyzeResponse
 * 使用@JsonProperty注解确保与Python API的蛇形命名一致
 */
@Data
public class AIAnalyzeResponse {

    @JsonProperty("visit_id")
    private Long visitId;

    @JsonProperty("risk_level")
    private String riskLevel;

    private String analysis;

    @JsonProperty("guideline_refs")
    private List<GuidelineRef> guidelineRefs;

    private List<String> suggestions;

    @JsonProperty("model_version")
    private String modelVersion;

    @JsonProperty("prompt_version")
    private String promptVersion;

    @JsonProperty("kb_version")
    private String kbVersion;

    private String timestamp;

    @Data
    public static class GuidelineRef {
        private String title;
        private String section;
    }
}