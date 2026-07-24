package com.medical.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * RAG检索响应DTO
 */
@Data
public class RetrieveResponse {

    private Boolean success;

    private String query;

    private List<CitationItem> results;

    private String context;

    private Integer total;

    @Data
    public static class CitationItem {
        @JsonProperty("chunk_id")
        private String chunkId;

        private String source;

        private String section;

        private String content;

        private Float score;
    }
}