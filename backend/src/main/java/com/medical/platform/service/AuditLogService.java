package com.medical.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.platform.entity.AuditLog;
import com.medical.platform.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 审计日志服务
 * 用于记录系统关键操作
 *
 * @author 贺孟缘
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * 记录审计日志
     *
     * @param eventType 事件类型
     * @param userId    用户ID
     * @param visitId   就诊ID（可选）
     * @param details   详细信息
     * @param ipAddress IP地址
     */
    @Transactional
    public void log(String eventType, Long userId, Long visitId, Object details, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEventType(eventType);
            auditLog.setUserId(userId);
            auditLog.setVisitId(visitId);
            auditLog.setIpAddress(ipAddress);

            // 将details对象转换为JSON字符串
            if (details != null) {
                if (details instanceof String) {
                    auditLog.setDetails((String) details);
                } else {
                    auditLog.setDetails(objectMapper.writeValueAsString(details));
                }
            }

            auditLogRepository.insert(auditLog);
            log.debug("审计日志记录成功: eventType={}, userId={}", eventType, userId);
        } catch (Exception e) {
            log.error("记录审计日志失败: eventType={}, userId={}", eventType, userId, e);
        }
    }

    /**
     * 记录登录日志
     */
    public void logLogin(Long userId, String username, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("username", username);
        details.put("action", "用户登录");
        log("LOGIN", userId, null, details, ipAddress);
    }

    /**
     * 记录退出登录日志
     */
    public void logLogout(Long userId, String username, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("username", username);
        details.put("action", "用户退出登录");
        log("LOGOUT", userId, null, details, ipAddress);
    }

    /**
     * 记录AI分析日志
     */
    public void logAIAnalyze(Long userId, Long visitId, String riskLevel, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("action", "AI分析");
        details.put("riskLevel", riskLevel);
        log("AI_ANALYZE", userId, visitId, details, ipAddress);
    }

    /**
     * 记录就诊记录创建日志
     */
    public void logVisitCreate(Long userId, Long visitId, String patientName, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("action", "创建就诊记录");
        details.put("patientName", patientName);
        log("VISIT_CREATE", userId, visitId, details, ipAddress);
    }

    /**
     * 记录就诊记录更新日志
     */
    public void logVisitUpdate(Long userId, Long visitId, String action, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("action", action);
        log("VISIT_UPDATE", userId, visitId, details, ipAddress);
    }

    /**
     * 分页查询审计日志
     *
     * @param eventType 事件类型（可选）
     * @param userId    用户ID（可选）
     * @param visitId   就诊ID（可选）
     * @param page      分页参数
     * @return 分页结果
     */
    public Page<AuditLog> getAuditLogs(String eventType, Long userId, Long visitId, Page<AuditLog> page) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();

        if (eventType != null && !eventType.isEmpty()) {
            wrapper.eq(AuditLog::getEventType, eventType);
        }
        if (userId != null) {
            wrapper.eq(AuditLog::getUserId, userId);
        }
        if (visitId != null) {
            wrapper.eq(AuditLog::getVisitId, visitId);
        }

        wrapper.orderByDesc(AuditLog::getCreatedAt);

        return auditLogRepository.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询审计日志详情
     *
     * @param id 日志ID
     * @return 审计日志
     */
    public AuditLog getAuditLogById(Long id) {
        return auditLogRepository.selectById(id);
    }
}