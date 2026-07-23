package com.medical.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * LLM推理请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMRequest {

    /**
     * Prompt模板名称
     */
    @JsonProperty("prompt_template")
    private String promptTemplate;

    /**
     * 模板变量值
     */
    private Map<String, Object> variables;

    /**
     * 最大生成token数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens = 2048;

    /**
     * 温度参数（0.0-1.0）
     */
    private Float temperature = 0.1f;

    /**
     * 是否流式输出
     */
    private Boolean stream = false;
}