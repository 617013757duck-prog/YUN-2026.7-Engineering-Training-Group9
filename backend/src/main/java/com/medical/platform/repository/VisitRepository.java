package com.medical.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.platform.entity.Visit;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VisitRepository extends BaseMapper<Visit> {

    List<Visit> findByPatientId(Long patientId);

    List<Visit> findByStatus(String status);

    List<Visit> findByRiskLevel(String riskLevel);
}