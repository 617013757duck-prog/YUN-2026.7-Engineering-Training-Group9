package com.medical.platform.entity;

public enum VisitStatus {

    PENDING("待处理", "就诊信息已提交，等待审核"),
    PRE_SCREENED("预筛查完成", "规则引擎预筛查完成"),
    AI_ANALYZED("AI分析完成", "AI分析完成，等待安全复核"),
    SAFETY_REVIEWED("安全复核完成", "安全Agent复核完成"),
    DOCTOR_REVIEWING("医生审核中", "医生正在审核"),
    DOCTOR_APPROVED("医生已审核", "医生审核通过"),
    FOLLOWUP_PLANNED("随访计划已制定", "已制定随访计划"),
    COMPLETED("已完成", "就诊流程已完成"),
    REJECTED("已拒绝", "就诊申请被拒绝");

    private final String displayName;
    private final String description;

    VisitStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}