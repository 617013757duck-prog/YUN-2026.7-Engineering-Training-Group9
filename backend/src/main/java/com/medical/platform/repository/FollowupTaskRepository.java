package com.medical.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.platform.entity.FollowupTask;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 随访任务Repository
 */
@Mapper
public interface FollowupTaskRepository extends BaseMapper<FollowupTask> {

    /**
     * 根据计划ID查询任务
     */
    List<FollowupTask> findByPlanId(Long planId);

    /**
     * 根据状态查询任务
     */
    List<FollowupTask> findByStatus(String status);

    /**
     * 查询指定日期范围内待执行的任务
     */
    List<FollowupTask> findByScheduledDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, String status);
}