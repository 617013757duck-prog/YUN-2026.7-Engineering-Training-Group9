package com.medical.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.platform.entity.Symptom;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SymptomRepository extends BaseMapper<Symptom> {

    List<Symptom> findByVisitId(Long visitId);

    List<Symptom> findByRedFlag(Integer isRedFlag);

    List<Symptom> findByVisitIdAndRedFlag(Long visitId, Integer isRedFlag);
}