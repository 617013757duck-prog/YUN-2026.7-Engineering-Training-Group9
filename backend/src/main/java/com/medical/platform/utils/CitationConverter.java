package com.medical.platform.utils;

import com.medical.platform.dto.RetrieveResponse;
import com.medical.platform.entity.Citation;

import java.util.ArrayList;
import java.util.List;

/**
 * Citation转换工具类
 * 用于将AI服务返回的CitationItem转换为数据库实体Citation
 */
public class CitationConverter {

    /**
     * 将RetrieveResponse.CitationItem转换为Citation实体
     *
     * @param item AI服务返回的引用项
     * @param visitId 就诊ID
     * @return Citation实体
     */
    public static Citation toEntity(RetrieveResponse.CitationItem item, Long visitId) {
        if (item == null) {
            return null;
        }

        Citation citation = new Citation();
        citation.setVisitId(visitId);
        
        // AI服务返回的字段映射到数据库字段
        // chunk_id -> 暂时不保存，因为数据库没有这个字段
        citation.setGuidelineId(null); // 如果有guideline_id可以从source推断
        
        // content -> citation_text
        citation.setCitationText(item.getContent());
        
        // section -> source_section
        if (item.getSection() != null) {
            citation.setSourceSection(item.getSection());
        } else if (item.getSource() != null) {
            citation.setSourceSection(item.getSource());
        }
        
        // score -> relevance_score
        if (item.getScore() != null) {
            citation.setRelevanceScore(java.math.BigDecimal.valueOf(item.getScore()));
        }
        
        return citation;
    }

    /**
     * 批量转换CitationItem列表为Citation实体列表
     *
     * @param items AI服务返回的引用项列表
     * @param visitId 就诊ID
     * @return Citation实体列表
     */
    public static List<Citation> toEntityList(List<RetrieveResponse.CitationItem> items, Long visitId) {
        List<Citation> citations = new ArrayList<>();
        
        if (items == null || items.isEmpty()) {
            return citations;
        }
        
        for (RetrieveResponse.CitationItem item : items) {
            Citation citation = toEntity(item, visitId);
            if (citation != null) {
                citations.add(citation);
            }
        }
        
        return citations;
    }
}