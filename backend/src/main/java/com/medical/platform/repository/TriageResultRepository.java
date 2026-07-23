package com.medical.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.platform.entity.TriageResult;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TriageResultRepository extends BaseMapper<TriageResult> {

    TriageResult findByVisitId(Long visitId);

    List<TriageResult> findByRiskLevel(String riskLevel);
}