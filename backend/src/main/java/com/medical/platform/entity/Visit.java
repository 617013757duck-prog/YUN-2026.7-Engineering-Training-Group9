package com.medical.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.medical.platform.annotation.SensitiveField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("visits")
public class Visit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;

    private String patientName;

    @SensitiveField(type = SensitiveField.SensitiveType.PHONE)
    private String patientPhone;

    @SensitiveField(type = SensitiveField.SensitiveType.ID_CARD)
    private String patientIdCard;

    private Integer age;

    private String gender;

    private String chiefComplaint;

    private String historyOfPresentIllness;

    private String pastMedicalHistory;

    private String medicationHistory;

    private String allergies;

    private String status;

    private String riskLevel;

    private String riskBasis;

    private String aiAnalysis;

    private Long medicalStaffId;

    private String medicalStaffComment;

    private String triageResult;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}