package com.medical.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.platform.entity.Citation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 引用记录Repository
 */
@Mapper
public interface CitationRepository extends BaseMapper<Citation> {

    /**
     * 根据就诊ID查询引用记录
     */
    List<Citation> findByVisitId(Long visitId);

    /**
     * 根据指南ID查询引用记录
     */
    List<Citation> findByGuidelineId(Long guidelineId);
}