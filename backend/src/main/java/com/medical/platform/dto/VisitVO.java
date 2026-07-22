package com.medical.platform.dto;

import com.medical.platform.entity.Symptom;
import com.medical.platform.entity.TriageResult;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VisitVO {

    private Long id;

    private String patientName;

    private String patientPhone;

    private String patientIdCard;

    private Integer age;

    private String gender;

    private String chiefComplaint;

    private String historyOfPresentIllness;

    private String pastMedicalHistory;

    private String medicationHistory;

    private String allergies;

    private String status;

    private String statusName;

    private String riskLevel;

    private String riskBasis;

    private String aiAnalysis;

    private String medicalStaffComment;

    private String triageResult;

    private List<Symptom> symptoms;

    private TriageResult triage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}