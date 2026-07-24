package com.medical.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.platform.entity.AgentRun;
import com.medical.platform.repository.AgentRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Agent运行记录服务
 * 用于记录和查询AI Agent的执行历史
 *
 * @author 贺孟缘
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentRunService {

    private final AgentRunRepository agentRunRepository;
    private final ObjectMapper objectMapper;

    /**
     * 记录Agent运行
     *
     * @param visitId          就诊ID
     * @param agentType        Agent类型
     * @param modelVersion     模型版本
     * @param promptVersion    Prompt版本
     * @param kbVersion        知识库版本
     * @param inputData        输入数据
     * @param outputData       输出数据
     * @param executionTimeMs  执行时间（毫秒）
     * @param status           执行状态
     * @param errorMessage     错误信息
     * @param triggeredBy      触发用户ID
     * @return AgentRun记录
     */
    @Transactional
    public AgentRun recordRun(Long visitId, String agentType, String modelVersion,
                               String promptVersion, String kbVersion,
                               Object inputData, Object outputData,
                               Integer executionTimeMs, String status,
                               String errorMessage, Long triggeredBy) {
        try {
            AgentRun run = new AgentRun();
            run.setVisitId(visitId);
            run.setAgentType(agentType);
            run.setModelVersion(modelVersion);
            run.setPromptVersion(promptVersion);
            run.setKnowledgeBaseVersion(kbVersion);
            run.setExecutionTimeMs(executionTimeMs);
            run.setStatus(status);
            run.setErrorMessage(errorMessage);
            run.setTriggeredBy(triggeredBy);

            // 转换输入输出数据为JSON字符串
            if (inputData != null) {
                run.setInputData(inputData instanceof String ? (String) inputData : objectMapper.writeValueAsString(inputData));
            }
            if (outputData != null) {
                run.setOutputData(outputData instanceof String ? (String) outputData : objectMapper.writeValueAsString(outputData));
            }

            agentRunRepository.insert(run);
            log.info("Agent运行记录保存成功: visitId={}, agentType={}, status={}", visitId, agentType, status);
            return run;
        } catch (Exception e) {
            log.error("保存Agent运行记录失败: visitId={}, agentType={}", visitId, agentType, e);
            return null;
        }
    }

    /**
     * 根据就诊ID查询Agent运行记录
     *
     * @param visitId 就诊ID
     * @return Agent运行记录列表
     */
    public List<AgentRun> getByVisitId(Long visitId) {
        return agentRunRepository.selectList(
                new LambdaQueryWrapper<AgentRun>()
                        .eq(AgentRun::getVisitId, visitId)
                        .orderByDesc(AgentRun::getCreateTime)
        );
    }

    /**
     * 分页查询Agent运行记录
     *
     * @param agentType Agent类型（可选）
     * @param status    执行状态（可选）
     * @param visitId   就诊ID（可选）
     * @param page      分页参数
     * @return 分页结果
     */
    public Page<AgentRun> getAgentRuns(String agentType, String status, Long visitId, Page<AgentRun> page) {
        LambdaQueryWrapper<AgentRun> wrapper = new LambdaQueryWrapper<>();

        if (agentType != null && !agentType.isEmpty()) {
            wrapper.eq(AgentRun::getAgentType, agentType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(AgentRun::getStatus, status);
        }
        if (visitId != null) {
            wrapper.eq(AgentRun::getVisitId, visitId);
        }

        wrapper.orderByDesc(AgentRun::getCreateTime);

        return agentRunRepository.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询Agent运行记录
     *
     * @param id 记录ID
     * @return Agent运行记录
     */
    public AgentRun getById(Long id) {
        return agentRunRepository.selectById(id);
    }

    /**
     * 记录成功的Agent运行
     */
    public AgentRun recordSuccess(Long visitId, String agentType, String modelVersion,
                                   String promptVersion, String kbVersion,
                                   Object inputData, Object outputData,
                                   Integer executionTimeMs, Long triggeredBy) {
        return recordRun(visitId, agentType, modelVersion, promptVersion, kbVersion,
                inputData, outputData, executionTimeMs, "SUCCESS", null, triggeredBy);
    }

    /**
     * 记录失败的Agent运行
     */
    public AgentRun recordFailure(Long visitId, String agentType, String modelVersion,
                                   String promptVersion, String kbVersion,
                                   Object inputData, String errorMessage,
                                   Integer executionTimeMs, Long triggeredBy) {
        return recordRun(visitId, agentType, modelVersion, promptVersion, kbVersion,
                inputData, null, executionTimeMs, "FAILED", errorMessage, triggeredBy);
    }
}