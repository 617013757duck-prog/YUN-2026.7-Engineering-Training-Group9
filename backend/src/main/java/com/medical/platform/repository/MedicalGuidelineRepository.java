package com.medical.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.platform.entity.MedicalGuideline;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 医疗指南Repository
 */
@Mapper
public interface MedicalGuidelineRepository extends BaseMapper<MedicalGuideline> {

    /**
     * 根据分类查询指南
     */
    List<MedicalGuideline> findByCategory(String category);

    /**
     * 根据状态查询指南
     */
    List<MedicalGuideline> findByStatus(Integer status);

    /**
     * 根据来源查询指南
     */
    List<MedicalGuideline> findBySource(String source);
}