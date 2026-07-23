package com.medical.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * RAG检索请求DTO
 */
@Data
public class RetrieveRequest {

    private String query;

    @JsonProperty("visit_id")
    private Long visitId;

    @JsonProperty("top_k")
    private Integer topK = 5;
}