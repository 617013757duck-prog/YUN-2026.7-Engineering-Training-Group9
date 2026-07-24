package com.medical.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.platform.entity.Citation;
import com.medical.platform.repository.CitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 引用记录服务
 * 管理AI分析时的引用记录
 *
 * @author 系统生成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CitationService {

    private final CitationRepository citationRepository;

    /**
     * 创建引用记录
     */
    @Transactional
    public Citation createCitation(Citation citation) {
        citationRepository.insert(citation);
        log.info("引用记录创建成功: visitId={}, guidelineId={}",
                citation.getVisitId(), citation.getGuidelineId());
        return citation;
    }

    /**
     * 根据就诊ID查询引用记录列表
     */
    public List<Citation> getCitationsByVisitId(Long visitId) {
        return citationRepository.findByVisitId(visitId);
    }

    /**
     * 分页查询引用记录
     */
    public Page<Citation> getCitations(Long visitId, Long agentRunId, Long guidelineId, Page<Citation> page) {
        LambdaQueryWrapper<Citation> wrapper = new LambdaQueryWrapper<>();

        if (visitId != null) {
            wrapper.eq(Citation::getVisitId, visitId);
        }
        if (agentRunId != null) {
            wrapper.eq(Citation::getAgentRunId, agentRunId);
        }
        if (guidelineId != null) {
            wrapper.eq(Citation::getGuidelineId, guidelineId);
        }

        wrapper.orderByDesc(Citation::getCreateTime);

        return citationRepository.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询引用记录
     */
    public Citation getCitationById(Long id) {
        return citationRepository.selectById(id);
    }

    /**
     * 删除引用记录
     */
    @Transactional
    public void deleteCitation(Long id) {
        citationRepository.deleteById(id);
        log.info("引用记录删除成功: id={}", id);
    }
}