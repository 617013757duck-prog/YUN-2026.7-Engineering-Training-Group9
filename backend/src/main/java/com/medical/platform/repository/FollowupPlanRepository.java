package com.medical.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.platform.entity.FollowupPlan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 随访计划Repository
 */
@Mapper
public interface FollowupPlanRepository extends BaseMapper<FollowupPlan> {

    /**
     * 根据患者ID查询随访计划
     */
    List<FollowupPlan> findByPatientId(Long patientId);

    /**
     * 根据状态查询随访计划
     */
    List<FollowupPlan> findByStatus(String status);

    /**
     * 根据计划类型查询
     */
    List<FollowupPlan> findByPlanType(String planType);
}