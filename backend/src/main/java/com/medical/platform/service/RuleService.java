package com.medical.platform.service;

import com.medical.platform.entity.SafetyAlert;
import com.medical.platform.entity.Symptom;
import com.medical.platform.entity.Visit;
import com.medical.platform.repository.SafetyAlertRepository;
import com.medical.platform.repository.SymptomRepository;
import com.medical.platform.repository.VisitRepository;
import com.medical.platform.rule.ContraindicationChecker;
import com.medical.platform.rule.RedFlagRuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 规则引擎服务
 * 整合红旗症状检查、禁忌条件检查、安全警报管理等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleService {

    private final RedFlagRuleEngine redFlagRuleEngine;
    private final ContraindicationChecker contraindicationChecker;
    private final VisitRepository visitRepository;
    private final SymptomRepository symptomRepository;
    private final SafetyAlertRepository safetyAlertRepository;

    /**
     * 对就诊记录进行完整的安全检查
     * 包括红旗症状检查、禁忌条件检查、创建安全警报
     *
     * @param visitId 就诊ID
     * @return 安全检查结果
     */
    @Transactional
    public SafetyCheckResult performSafetyCheck(Long visitId) {
        SafetyCheckResult result = new SafetyCheckResult();

        // 1. 获取就诊记录
        Visit visit = visitRepository.selectById(visitId);
        if (visit == null) {
            log.warn("就诊记录不存在: visitId={}", visitId);
            return result;
        }

        // 2. 获取症状列表
        List<Symptom> symptoms = symptomRepository.findByVisitId(visitId);

        // 3. 执行红旗症状检查
        RedFlagRuleEngine.RedFlagCheckResult redFlagResult = redFlagRuleEngine.checkRedFlags(symptoms);
        result.setRedFlagResult(redFlagResult);

        // 4. 执行禁忌条件检查
        ContraindicationChecker.ContraindicationCheckResult contraindicationResult =
            contraindicationChecker.checkContraindications(visit);
        result.setContraindicationResult(contraindicationResult);

        // 5. 创建安全警报（如果有红旗症状或禁忌条件）
        if (redFlagResult.isHasRedFlag()) {
            createRedFlagAlerts(visitId, redFlagResult);
        }

        if (contraindicationResult.isHasContraindication()) {
            createContraindicationAlerts(visitId, contraindicationResult);
        }

        // 6. 更新就诊记录的风险等级
        updateVisitRiskLevel(visit, redFlagResult, contraindicationResult);

        result.setVisitId(visitId);
        result.setHasSafetyIssue(redFlagResult.isHasRedFlag() || contraindicationResult.isHasContraindication());

        log.info("安全检查完成: visitId={}, 红旗症状={}, 禁忌条件={}",
            visitId, redFlagResult.getRedFlagCount(), contraindicationResult.getWarningCount());

        return result;
    }

    /**
     * 创建红旗症状安全警报
     */
    private void createRedFlagAlerts(Long visitId, RedFlagRuleEngine.RedFlagCheckResult redFlagResult) {
        for (RedFlagRuleEngine.RedFlagMatch match : redFlagResult.getRedFlagMatches()) {
            SafetyAlert alert = new SafetyAlert();
            alert.setVisitId(visitId);
            alert.setAlertType("红旗症状");
            alert.setAlertLevel(match.getSeverity());
            alert.setAlertContent(String.format("发现红旗症状: %s (关键词: %s), 可能疾病: %s, 建议: %s",
                match.getSymptomName(), match.getKeyword(), match.getPossibleDisease(), match.getUrgency()));
            alert.setTriggerSource("RedFlagRuleEngine");
            alert.setStatus(0); // 未处理
            safetyAlertRepository.insert(alert);
        }
    }

    /**
     * 创建禁忌条件安全警报
     */
    private void createContraindicationAlerts(Long visitId,
            ContraindicationChecker.ContraindicationCheckResult contraindicationResult) {
        for (ContraindicationChecker.ContraindicationWarning warning : contraindicationResult.getWarnings()) {
            SafetyAlert alert = new SafetyAlert();
            alert.setVisitId(visitId);
            alert.setAlertType(warning.getType());
            alert.setAlertLevel(warning.getLevel());
            alert.setAlertContent(String.format("发现禁忌条件: %s, 建议: %s",
                warning.getContent(), warning.getSuggestion()));
            alert.setTriggerSource("ContraindicationChecker");
            alert.setStatus(0); // 未处理
            safetyAlertRepository.insert(alert);
        }
    }

    /**
     * 更新就诊记录的风险等级
     */
    private void updateVisitRiskLevel(Visit visit,
            RedFlagRuleEngine.RedFlagCheckResult redFlagResult,
            ContraindicationChecker.ContraindicationCheckResult contraindicationResult) {

        String riskLevel = "低风险";

        // 根据红旗症状和禁忌条件确定风险等级
        if (redFlagResult.isHasRedFlag()) {
            String maxRisk = redFlagResult.getMaxRiskLevel();
            if ("高".equals(maxRisk)) {
                riskLevel = "高风险";
            } else if ("中".equals(maxRisk)) {
                riskLevel = "中风险";
            }
        }

        // 如果有高级别禁忌条件，提升风险等级
        if (contraindicationResult.isHasContraindication()) {
            boolean hasHighLevelWarning = contraindicationResult.getWarnings().stream()
                .anyMatch(w -> "高".equals(w.getLevel()));

            if (hasHighLevelWarning && "低风险".equals(riskLevel)) {
                riskLevel = "中风险";
            } else if (hasHighLevelWarning && "中风险".equals(riskLevel)) {
                riskLevel = "高风险";
            }
        }

        visit.setRiskLevel(riskLevel);
        visit.setRiskBasis("基于红旗症状和禁忌条件检查自动评估");
        visitRepository.updateById(visit);

        log.info("更新就诊记录风险等级: visitId={}, riskLevel={}", visit.getId(), riskLevel);
    }

    /**
     * 获取就诊记录的安全警报列表
     */
    public List<SafetyAlert> getSafetyAlerts(Long visitId) {
        return safetyAlertRepository.findByVisitId(visitId);
    }

    /**
     * 处理安全警报
     */
    @Transactional
    public SafetyAlert handleSafetyAlert(Long alertId, String handledBy, String handleResult) {
        SafetyAlert alert = safetyAlertRepository.selectById(alertId);
        if (alert == null) {
            log.warn("安全警报不存在: alertId={}", alertId);
            return null;
        }

        alert.setStatus(1); // 已处理
        alert.setHandledBy(handledBy);
        alert.setHandleResult(handleResult);
        safetyAlertRepository.updateById(alert);

        log.info("处理安全警报: alertId={}, handledBy={}", alertId, handledBy);
        return alert;
    }

    /**
     * 安全检查结果
     */
    public static class SafetyCheckResult {
        private Long visitId;
        private boolean hasSafetyIssue;
        private RedFlagRuleEngine.RedFlagCheckResult redFlagResult;
        private ContraindicationChecker.ContraindicationCheckResult contraindicationResult;

        // Getters and Setters
        public Long getVisitId() { return visitId; }
        public void setVisitId(Long visitId) { this.visitId = visitId; }
        public boolean isHasSafetyIssue() { return hasSafetyIssue; }
        public void setHasSafetyIssue(boolean hasSafetyIssue) { this.hasSafetyIssue = hasSafetyIssue; }
        public RedFlagRuleEngine.RedFlagCheckResult getRedFlagResult() { return redFlagResult; }
        public void setRedFlagResult(RedFlagRuleEngine.RedFlagCheckResult redFlagResult) { this.redFlagResult = redFlagResult; }
        public ContraindicationChecker.ContraindicationCheckResult getContraindicationResult() { return contraindicationResult; }
        public void setContraindicationResult(ContraindicationChecker.ContraindicationCheckResult contraindicationResult) { this.contraindicationResult = contraindicationResult; }
    }
}