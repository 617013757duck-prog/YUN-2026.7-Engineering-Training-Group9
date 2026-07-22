package com.medical.platform.dto;

import lombok.Data;

import java.util.List;

@Data
public class VisitCreateRequest {

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

    private List<SymptomRequest> symptoms;
}