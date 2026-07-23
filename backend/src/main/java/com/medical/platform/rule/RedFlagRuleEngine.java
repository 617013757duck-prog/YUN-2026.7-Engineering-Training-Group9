package com.medical.platform.rule;

import com.medical.platform.entity.Symptom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 红旗症状规则引擎
 * 用于识别可能表示严重疾病或需要立即就医的红旗症状
 */
@Slf4j
@Component
public class RedFlagRuleEngine {

    /**
     * 红旗症状关键词映射表
     * key: 症状关键词
     * value: 红旗症状信息（严重程度、可能疾病、紧急程度）
     */
    private static final Map<String, RedFlagInfo> RED_FLAG_KEYWORDS = new HashMap<>();

    static {
        // 心血管系统红旗症状
        RED_FLAG_KEYWORDS.put("胸痛", new RedFlagInfo("高", "心血管疾病", "立即就医"));
        RED_FLAG_KEYWORDS.put("胸闷", new RedFlagInfo("高", "心血管疾病", "立即就医"));
        RED_FLAG_KEYWORDS.put("心悸", new RedFlagInfo("中", "心血管疾病", "尽快就医"));
        RED_FLAG_KEYWORDS.put("心前区疼痛", new RedFlagInfo("高", "心血管疾病", "立即就医"));

        // 呼吸系统红旗症状
        RED_FLAG_KEYWORDS.put("呼吸困难", new RedFlagInfo("高", "呼吸系统疾病", "立即就医"));
        RED_FLAG_KEYWORDS.put("气促", new RedFlagInfo("中", "呼吸系统疾病", "尽快就医"));
        RED_FLAG_KEYWORDS.put("咯血", new RedFlagInfo("高", "呼吸系统疾病", "立即就医"));
        RED_FLAG_KEYWORDS.put("咳血", new RedFlagInfo("高", "呼吸系统疾病", "立即就医"));

        // 神经系统红旗症状
        RED_FLAG_KEYWORDS.put("剧烈头痛", new RedFlagInfo("高", "神经系统疾病", "立即就医"));
        RED_FLAG_KEYWORDS.put("突发头痛", new RedFlagInfo("高", "神经系统疾病", "立即就医"));
        RED_FLAG_KEYWORDS.put("意识模糊", new RedFlagInfo("高", "神经系统疾病", "立即就医"));
        RED_FLAG_KEYWORDS.put("昏迷", new RedFlagInfo("高", "神经系统疾病", "立即就医"));
        RED_FLAG_KEYWORDS.put("肢体麻木", new RedFlagInfo("中", "神经系统疾病", "尽快就医"));
        RED_FLAG_KEYWORDS.put("口角歪斜", new RedFlagInfo("高", "神经系统疾病", "立即就医"));

        // 消化系统红旗症状
        RED_FLAG_KEYWORDS.put("呕血", new RedFlagInfo("高", "消化系统疾病", "立即就医"));
        RED_FLAG_KEYWORDS.put("便血", new RedFlagInfo("高", "消化系统疾病", "立即就医"));
        RED_FLAG_KEYWORDS.put("黑便", new RedFlagInfo("高", "消化系统疾病", "立即就医"));
        RED_FLAG_KEYWORDS.put("剧烈腹痛", new RedFlagInfo("高", "消化系统疾病", "立即就医"));
        RED_FLAG_KEYWORDS.put("急腹症", new RedFlagInfo("高", "消化系统疾病", "立即就医"));

        // 眼科红旗症状
        RED_FLAG_KEYWORDS.put("突发视力丧失", new RedFlagInfo("高", "眼科急症", "立即就医"));
        RED_FLAG_KEYWORDS.put("视力模糊", new RedFlagInfo("中", "眼科疾病", "尽快就医"));
        RED_FLAG_KEYWORDS.put("视野缺损", new RedFlagInfo("高", "眼科急症", "立即就医"));

        // 全身性红旗症状
        RED_FLAG_KEYWORDS.put("持续高烧", new RedFlagInfo("高", "严重感染", "立即就医"));
        RED_FLAG_KEYWORDS.put("高热", new RedFlagInfo("中", "感染性疾病", "尽快就医"));
        RED_FLAG_KEYWORDS.put("寒战", new RedFlagInfo("中", "感染性疾病", "尽快就医"));
        RED_FLAG_KEYWORDS.put("休克", new RedFlagInfo("高", "危急重症", "立即就医"));

        // 其他红旗症状
        RED_FLAG_KEYWORDS.put("过敏反应", new RedFlagInfo("高", "过敏急症", "立即就医"));
        RED_FLAG_KEYWORDS.put("严重过敏", new RedFlagInfo("高", "过敏急症", "立即就医"));
        RED_FLAG_KEYWORDS.put("药物过敏", new RedFlagInfo("高", "过敏急症", "立即就医"));
    }

    /**
     * 检查症状列表中的红旗症状
     *
     * @param symptoms 症状列表
     * @return 红旗症状检查结果
     */
    public RedFlagCheckResult checkRedFlags(List<Symptom> symptoms) {
        RedFlagCheckResult result = new RedFlagCheckResult();
        List<RedFlagMatch> matches = new ArrayList<>();

        if (symptoms == null || symptoms.isEmpty()) {
            return result;
        }

        for (Symptom symptom : symptoms) {
            String symptomName = symptom.getSymptomName();
            String description = symptom.getDescription();

            // 检查症状名称
            RedFlagMatch nameMatch = matchRedFlag(symptomName);
            if (nameMatch != null) {
                nameMatch.setSymptomId(symptom.getId());
                nameMatch.setSymptomName(symptomName);
                matches.add(nameMatch);
                symptom.setIsRedFlag(1);
                continue;
            }

            // 检查症状描述
            RedFlagMatch descMatch = matchRedFlag(description);
            if (descMatch != null) {
                descMatch.setSymptomId(symptom.getId());
                descMatch.setSymptomName(symptomName);
                matches.add(descMatch);
                symptom.setIsRedFlag(1);
            }
        }

        result.setRedFlagMatches(matches);
        result.setHasRedFlag(!matches.isEmpty());
        result.setRedFlagCount(matches.size());

        // 确定最高风险级别
        if (!matches.isEmpty()) {
            result.setMaxRiskLevel(matches.stream()
                .map(RedFlagMatch::getSeverity)
                .max(Comparator.comparing(this::getSeverityLevel))
                .orElse("低"));
        }

        log.info("红旗症状检查完成: 发现{}个红旗症状, 最高风险级别={}", matches.size(), result.getMaxRiskLevel());
        return result;
    }

    /**
     * 匹配红旗症状关键词
     */
    private RedFlagMatch matchRedFlag(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        for (Map.Entry<String, RedFlagInfo> entry : RED_FLAG_KEYWORDS.entrySet()) {
            if (text.contains(entry.getKey())) {
                RedFlagMatch match = new RedFlagMatch();
                match.setKeyword(entry.getKey());
                match.setSeverity(entry.getValue().severity);
                match.setPossibleDisease(entry.getValue().possibleDisease);
                match.setUrgency(entry.getValue().urgency);
                return match;
            }
        }
        return null;
    }

    /**
     * 获取严重程度级别（用于排序）
     */
    private int getSeverityLevel(String severity) {
        switch (severity) {
            case "高":
                return 3;
            case "中":
                return 2;
            case "低":
                return 1;
            default:
                return 0;
        }
    }

    /**
     * 红旗症状信息
     */
    private static class RedFlagInfo {
        String severity;        // 严重程度
        String possibleDisease; // 可能疾病
        String urgency;         // 紧急程度

        RedFlagInfo(String severity, String possibleDisease, String urgency) {
            this.severity = severity;
            this.possibleDisease = possibleDisease;
            this.urgency = urgency;
        }
    }

    /**
     * 红旗症状匹配结果
     */
    public static class RedFlagMatch {
        private Long symptomId;
        private String symptomName;
        private String keyword;
        private String severity;
        private String possibleDisease;
        private String urgency;

        // Getters and Setters
        public Long getSymptomId() { return symptomId; }
        public void setSymptomId(Long symptomId) { this.symptomId = symptomId; }
        public String getSymptomName() { return symptomName; }
        public void setSymptomName(String symptomName) { this.symptomName = symptomName; }
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getPossibleDisease() { return possibleDisease; }
        public void setPossibleDisease(String possibleDisease) { this.possibleDisease = possibleDisease; }
        public String getUrgency() { return urgency; }
        public void setUrgency(String urgency) { this.urgency = urgency; }
    }

    /**
     * 红旗症状检查结果
     */
    public static class RedFlagCheckResult {
        private boolean hasRedFlag;
        private int redFlagCount;
        private List<RedFlagMatch> redFlagMatches;
        private String maxRiskLevel;

        // Getters and Setters
        public boolean isHasRedFlag() { return hasRedFlag; }
        public void setHasRedFlag(boolean hasRedFlag) { this.hasRedFlag = hasRedFlag; }
        public int getRedFlagCount() { return redFlagCount; }
        public void setRedFlagCount(int redFlagCount) { this.redFlagCount = redFlagCount; }
        public List<RedFlagMatch> getRedFlagMatches() { return redFlagMatches; }
        public void setRedFlagMatches(List<RedFlagMatch> redFlagMatches) { this.redFlagMatches = redFlagMatches; }
        public String getMaxRiskLevel() { return maxRiskLevel; }
        public void setMaxRiskLevel(String maxRiskLevel) { this.maxRiskLevel = maxRiskLevel; }
    }
}