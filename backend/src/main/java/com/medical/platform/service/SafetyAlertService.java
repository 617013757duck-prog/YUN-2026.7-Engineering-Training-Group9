package com.medical.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.platform.entity.SafetyAlert;
import com.medical.platform.repository.SafetyAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 安全警报服务
 * 用于管理安全警报的创建、查询和处理
 *
 * @author 贺孟缘
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyAlertService {

    private final SafetyAlertRepository safetyAlertRepository;

    /**
     * 创建安全警报
     */
    @Transactional
    public SafetyAlert createAlert(SafetyAlert alert) {
        safetyAlertRepository.insert(alert);
        log.info("安全警报创建成功: visitId={}, alertType={}, alertLevel={}",
                alert.getVisitId(), alert.getAlertType(), alert.getAlertLevel());
        return alert;
    }

    /**
     * 处理安全警报
     */
    @Transactional
    public SafetyAlert handleAlert(Long id, String handler, String handleResult) {
        SafetyAlert alert = safetyAlertRepository.selectById(id);
        if (alert == null) {
            log.warn("安全警报不存在: id={}", id);
            return null;
        }

        alert.setStatus(1); // 已处理
        alert.setHandledBy(handler);
        alert.setHandleResult(handleResult);

        safetyAlertRepository.updateById(alert);
        log.info("安全警报处理成功: id={}, handler={}", id, handler);
        return alert;
    }

    /**
     * 分页查询安全警报
     */
    public Page<SafetyAlert> getAlerts(String alertLevel, Integer status, Long visitId, Page<SafetyAlert> page) {
        LambdaQueryWrapper<SafetyAlert> wrapper = new LambdaQueryWrapper<>();

        if (alertLevel != null && !alertLevel.isEmpty()) {
            wrapper.eq(SafetyAlert::getAlertLevel, alertLevel);
        }
        if (status != null) {
            wrapper.eq(SafetyAlert::getStatus, status);
        }
        if (visitId != null) {
            wrapper.eq(SafetyAlert::getVisitId, visitId);
        }

        wrapper.orderByDesc(SafetyAlert::getCreateTime);

        return safetyAlertRepository.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询安全警报
     */
    public SafetyAlert getById(Long id) {
        return safetyAlertRepository.selectById(id);
    }

    /**
     * 删除安全警报
     */
    @Transactional
    public void deleteAlert(Long id) {
        safetyAlertRepository.deleteById(id);
        log.info("安全警报删除成功: id={}", id);
    }
}