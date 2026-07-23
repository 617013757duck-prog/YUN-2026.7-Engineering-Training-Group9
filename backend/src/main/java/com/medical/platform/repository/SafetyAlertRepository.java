package com.medical.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.platform.entity.SafetyAlert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 安全警报Repository
 */
@Mapper
public interface SafetyAlertRepository extends BaseMapper<SafetyAlert> {

    /**
     * 根据就诊ID查询警报
     */
    List<SafetyAlert> findByVisitId(Long visitId);

    /**
     * 根据警报级别查询
     */
    List<SafetyAlert> findByAlertLevel(String alertLevel);

    /**
     * 根据状态查询警报
     */
    List<SafetyAlert> findByStatus(Integer status);

    /**
     * 根据就诊ID和状态查询警报
     */
    List<SafetyAlert> findByVisitIdAndStatus(Long visitId, Integer status);
}