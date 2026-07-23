package com.medical.platform.rule;

import com.medical.platform.entity.Visit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 禁忌条件检查器
 * 用于检查患者的药物过敏、禁忌症、特殊人群等情况
 */
@Slf4j
@Component
public class ContraindicationChecker {

    /**
     * 常见过敏药物列表
     */
    private static final Set<String> COMMON_ALLERGENIC_DRUGS = new HashSet<>(Arrays.asList(
        "青霉素", "阿莫西林", "头孢", "磺胺", "阿司匹林", "布洛芬",
        "碘造影剂", "链霉素", "庆大霉素", "环丙沙星", "万古霉素"
    ));

    /**
     * 特殊人群关键词
     */
    private static final Map<String, String> SPECIAL_POPULATIONS = new HashMap<>();

    static {
        SPECIAL_POPULATIONS.put("孕妇", "妊娠期");
        SPECIAL_POPULATIONS.put("妊娠", "妊娠期");
        SPECIAL_POPULATIONS.put("哺乳", "哺乳期");
        SPECIAL_POPULATIONS.put("婴幼儿", "婴幼儿");
        SPECIAL_POPULATIONS.put("儿童", "儿童");
        SPECIAL_POPULATIONS.put("老年", "老年患者");
        SPECIAL_POPULATIONS.put("肝功能不全", "肝功能异常");
        SPECIAL_POPULATIONS.put("肾功能不全", "肾功能异常");
        SPECIAL_POPULATIONS.put("肾功能衰竭", "肾功能异常");
        SPECIAL_POPULATIONS.put("心脏病", "心脏病患者");
        SPECIAL_POPULATIONS.put("高血压", "高血压患者");
        SPECIAL_POPULATIONS.put("糖尿病", "糖尿病患者");
    }

    /**
     * 检查禁忌条件
     *
     * @param visit 就诊记录
     * @return 禁忌条件检查结果
     */
    public ContraindicationCheckResult checkContraindications(Visit visit) {
        ContraindicationCheckResult result = new ContraindicationCheckResult();
        List<ContraindicationWarning> warnings = new ArrayList<>();

        if (visit == null) {
            return result;
        }

        // 1. 检查药物过敏
        List<String> allergies = parseAllergies(visit.getAllergies());
        for (String allergy : allergies) {
            if (COMMON_ALLERGENIC_DRUGS.contains(allergy)) {
                ContraindicationWarning warning = new ContraindicationWarning();
                warning.setType("药物过敏");
                warning.setLevel("高");
                warning.setContent("患者对" + allergy + "过敏");
                warning.setSuggestion("避免使用" + allergy + "类药物");
                warnings.add(warning);
            }
        }

        // 2. 检查特殊人群
        checkSpecialPopulations(visit, warnings);

        // 3. 检查年龄相关禁忌
        checkAgeRelatedContraindications(visit, warnings);

        // 4. 检查病史中的禁忌症
        checkMedicalHistory(visit, warnings);

        result.setWarnings(warnings);
        result.setHasContraindication(!warnings.isEmpty());
        result.setWarningCount(warnings.size());

        log.info("禁忌条件检查完成: 发现{}个禁忌警告", warnings.size());
        return result;
    }

    /**
     * 解析过敏史
     */
    private List<String> parseAllergies(String allergies) {
        if (allergies == null || allergies.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> allergyList = new ArrayList<>();
        String[] items = allergies.split("[,，;；\\s]+");
        for (String item : items) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                allergyList.add(trimmed);
            }
        }
        return allergyList;
    }

    /**
     * 检查特殊人群
     */
    private void checkSpecialPopulations(Visit visit, List<ContraindicationWarning> warnings) {
        String medicalHistory = visit.getPastMedicalHistory();
        String medicationHistory = visit.getMedicationHistory();

        String textToCheck = (medicalHistory != null ? medicalHistory : "") + " " +
                            (medicationHistory != null ? medicationHistory : "");

        for (Map.Entry<String, String> entry : SPECIAL_POPULATIONS.entrySet()) {
            if (textToCheck.contains(entry.getKey())) {
                ContraindicationWarning warning = new ContraindicationWarning();
                warning.setType("特殊人群");
                warning.setLevel("中");
                warning.setContent("患者属于" + entry.getValue());
                warning.setSuggestion("用药需谨慎，需考虑" + entry.getValue() + "的特殊情况");
                warnings.add(warning);
            }
        }

        // 检查年龄相关的特殊人群
        if (visit.getAge() != null) {
            if (visit.getAge() >= 65) {
                ContraindicationWarning warning = new ContraindicationWarning();
                warning.setType("特殊人群");
                warning.setLevel("中");
                warning.setContent("患者年龄为" + visit.getAge() + "岁，属于老年患者");
                warning.setSuggestion("老年患者用药需调整剂量，注意肝肾功能");
                warnings.add(warning);
            } else if (visit.getAge() <= 3) {
                ContraindicationWarning warning = new ContraindicationWarning();
                warning.setType("特殊人群");
                warning.setLevel("高");
                warning.setContent("患者年龄为" + visit.getAge() + "岁，属于婴幼儿");
                warning.setSuggestion("婴幼儿用药需特别注意安全性和剂量");
                warnings.add(warning);
            }
        }
    }

    /**
     * 检查年龄相关禁忌
     */
    private void checkAgeRelatedContraindications(Visit visit, List<ContraindicationWarning> warnings) {
        if (visit.getAge() == null) {
            return;
        }

        Integer age = visit.getAge();

        // 儿童禁忌药物提醒
        if (age < 18) {
            ContraindicationWarning warning = new ContraindicationWarning();
            warning.setType("年龄禁忌");
            warning.setLevel("中");
            warning.setContent("患者为未成年人（" + age + "岁）");
            warning.setSuggestion("避免使用喹诺酮类、四环素类等儿童禁忌药物");
            warnings.add(warning);
        }

        // 老年人禁忌药物提醒
        if (age >= 60) {
            ContraindicationWarning warning = new ContraindicationWarning();
            warning.setType("年龄禁忌");
            warning.setLevel("中");
            warning.setContent("患者为老年人（" + age + "岁）");
            warning.setSuggestion("注意老年患者用药安全性，避免使用易致跌倒、意识模糊的药物");
            warnings.add(warning);
        }
    }

    /**
     * 检查病史中的禁忌症
     */
    private void checkMedicalHistory(Visit visit, List<ContraindicationWarning> warnings) {
        String pastHistory = visit.getPastMedicalHistory();

        if (pastHistory == null || pastHistory.isEmpty()) {
            return;
        }

        // 检查严重病史
        if (pastHistory.contains("消化道溃疡") || pastHistory.contains("胃溃疡")) {
            ContraindicationWarning warning = new ContraindicationWarning();
            warning.setType("病史禁忌");
            warning.setLevel("高");
            warning.setContent("患者有消化道溃疡病史");
            warning.setSuggestion("慎用NSAIDs类药物，避免加重溃疡");
            warnings.add(warning);
        }

        if (pastHistory.contains("哮喘")) {
            ContraindicationWarning warning = new ContraindicationWarning();
            warning.setType("病史禁忌");
            warning.setLevel("高");
            warning.setContent("患者有哮喘病史");
            warning.setSuggestion("慎用β受体阻滞剂，避免诱发哮喘");
            warnings.add(warning);
        }

        if (pastHistory.contains("青光眼")) {
            ContraindicationWarning warning = new ContraindicationWarning();
            warning.setType("病史禁忌");
            warning.setLevel("高");
            warning.setContent("患者有青光眼病史");
            warning.setSuggestion("慎用抗胆碱能药物、扩瞳药物");
            warnings.add(warning);
        }
    }

    /**
     * 禁忌条件警告
     */
    public static class ContraindicationWarning {
        private String type;       // 警告类型
        private String level;      // 警告级别
        private String content;    // 警告内容
        private String suggestion; // 建议

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }

    /**
     * 禁忌条件检查结果
     */
    public static class ContraindicationCheckResult {
        private boolean hasContraindication;
        private int warningCount;
        private List<ContraindicationWarning> warnings;

        // Getters and Setters
        public boolean isHasContraindication() { return hasContraindication; }
        public void setHasContraindication(boolean hasContraindication) { this.hasContraindication = hasContraindication; }
        public int getWarningCount() { return warningCount; }
        public void setWarningCount(int warningCount) { this.warningCount = warningCount; }
        public List<ContraindicationWarning> getWarnings() { return warnings; }
        public void setWarnings(List<ContraindicationWarning> warnings) { this.warnings = warnings; }
    }
}