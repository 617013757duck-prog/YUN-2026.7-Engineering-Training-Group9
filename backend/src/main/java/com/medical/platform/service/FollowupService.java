package com.medical.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medical.platform.entity.FollowupPlan;
import com.medical.platform.entity.FollowupTask;
import com.medical.platform.exception.BusinessException;
import com.medical.platform.repository.FollowupPlanRepository;
import com.medical.platform.repository.FollowupTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 随访管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowupService {

    private final FollowupPlanRepository planRepository;
    private final FollowupTaskRepository taskRepository;

    /**
     * 创建随访计划
     */
    @Transactional
    public FollowupPlan createPlan(FollowupPlan plan) {
        // 生成计划编号
        if (plan.getPlanCode() == null || plan.getPlanCode().isEmpty()) {
            plan.setPlanCode(generatePlanCode());
        }
        
        plan.setStatus("ACTIVE");
        planRepository.insert(plan);
        
        log.info("创建随访计划: planId={}, patientId={}, type={}", 
            plan.getId(), plan.getPatientId(), plan.getPlanType());
        return plan;
    }

    /**
     * 更新随访计划
     */
    @Transactional
    public FollowupPlan updatePlan(Long id, FollowupPlan planUpdate) {
        FollowupPlan plan = planRepository.selectById(id);
        if (plan == null) {
            throw new BusinessException(404, "随访计划不存在");
        }
        
        // 更新字段
        if (planUpdate.getPlanName() != null) {
            plan.setPlanName(planUpdate.getPlanName());
        }
        if (planUpdate.getEndDate() != null) {
            plan.setEndDate(planUpdate.getEndDate());
        }
        if (planUpdate.getFrequency() != null) {
            plan.setFrequency(planUpdate.getFrequency());
        }
        if (planUpdate.getStatus() != null) {
            plan.setStatus(planUpdate.getStatus());
        }
        
        planRepository.updateById(plan);
        log.info("更新随访计划: planId={}", id);
        return plan;
    }

    /**
     * 获取患者随访计划列表
     */
    public List<FollowupPlan> getPatientPlans(Long patientId) {
        return planRepository.findByPatientId(patientId);
    }

    /**
     * 获取随访计划详情
     */
    public FollowupPlan getPlanById(Long id) {
        FollowupPlan plan = planRepository.selectById(id);
        if (plan == null) {
            throw new BusinessException(404, "随访计划不存在");
        }
        return plan;
    }

    /**
     * 创建随访任务
     */
    @Transactional
    public FollowupTask createTask(FollowupTask task) {
        // 验证计划是否存在
        FollowupPlan plan = planRepository.selectById(task.getPlanId());
        if (plan == null) {
            throw new BusinessException(404, "随访计划不存在");
        }
        
        // 生成任务编号
        if (task.getTaskCode() == null || task.getTaskCode().isEmpty()) {
            task.setTaskCode(generateTaskCode());
        }
        
        task.setStatus("PENDING");
        taskRepository.insert(task);
        
        log.info("创建随访任务: taskId={}, planId={}, scheduledDate={}", 
            task.getId(), task.getPlanId(), task.getScheduledDate());
        return task;
    }

    /**
     * 完成随访任务
     */
    @Transactional
    public FollowupTask completeTask(Long id, Long executorId, String result, String notes) {
        FollowupTask task = taskRepository.selectById(id);
        if (task == null) {
            throw new BusinessException(404, "随访任务不存在");
        }
        
        task.setStatus("COMPLETED");
        task.setActualDate(LocalDate.now());
        task.setExecutorId(executorId);
        task.setResult(result);
        task.setNotes(notes);
        
        taskRepository.updateById(task);
        
        log.info("完成随访任务: taskId={}, executorId={}", id, executorId);
        return task;
    }

    /**
     * 获取随访计划的任务列表
     */
    public List<FollowupTask> getPlanTasks(Long planId) {
        return taskRepository.findByPlanId(planId);
    }

    /**
     * 获取待执行的随访任务（今天或之前未完成的任务）
     */
    public List<FollowupTask> getPendingTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.findByScheduledDateBetweenAndStatus(
            LocalDate.of(2020, 1, 1), today, "PENDING");
    }

    /**
     * 获取今日随访任务
     */
    public List<FollowupTask> getTodayTasks() {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<FollowupTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowupTask::getScheduledDate, today);
        wrapper.eq(FollowupTask::getStatus, "PENDING");
        return taskRepository.selectList(wrapper);
    }

    /**
     * 自动生成随访任务（根据频率）
     */
    @Transactional
    public void generateTasksForPlan(Long planId) {
        FollowupPlan plan = planRepository.selectById(planId);
        if (plan == null || !"ACTIVE".equals(plan.getStatus())) {
            return;
        }
        
        // 计算下一个任务日期
        LocalDate nextDate = calculateNextDate(plan);
        if (nextDate == null || (plan.getEndDate() != null && nextDate.isAfter(plan.getEndDate()))) {
            return;
        }
        
        // 创建任务
        FollowupTask task = new FollowupTask();
        task.setPlanId(planId);
        task.setScheduledDate(nextDate);
        task.setStatus("PENDING");
        task.setTaskCode(generateTaskCode());
        task.setTaskContent("定期随访");
        
        taskRepository.insert(task);
        log.info("自动生成随访任务: planId={}, scheduledDate={}", planId, nextDate);
    }

    /**
     * 计算下一个任务日期
     */
    private LocalDate calculateNextDate(FollowupPlan plan) {
        LocalDate today = LocalDate.now();
        String frequency = plan.getFrequency();
        
        switch (frequency) {
            case "DAILY":
                return today.plusDays(1);
            case "WEEKLY":
                return today.plusWeeks(1);
            case "MONTHLY":
                return today.plusMonths(1);
            default:
                return today.plusDays(1);
        }
    }

    /**
     * 生成计划编号
     */
    private String generatePlanCode() {
        return "FP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * 生成任务编号
     */
    private String generateTaskCode() {
        return "FT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * 删除随访计划
     */
    @Transactional
    public void deletePlan(Long id) {
        FollowupPlan plan = planRepository.selectById(id);
        if (plan == null) {
            throw new BusinessException(404, "随访计划不存在");
        }
        
        // 软删除
        planRepository.deleteById(id);
        
        // 同时删除相关任务
        taskRepository.delete(new LambdaQueryWrapper<FollowupTask>()
            .eq(FollowupTask::getPlanId, id));
        
        log.info("删除随访计划: planId={}", id);
    }
}