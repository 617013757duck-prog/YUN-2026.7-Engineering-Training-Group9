package com.medical.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * LLM推理响应DTO
 */
@Data
public class LLMResponse {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 使用的模板名称
     */
    @JsonProperty("template_used")
    private String templateUsed;

    /**
     * 模板版本
     */
    @JsonProperty("template_version")
    private String templateVersion;

    /**
     * 生成的文本
     */
    @JsonProperty("generated_text")
    private String generatedText;

    /**
     * 使用的模型名称
     */
    private String model;
}