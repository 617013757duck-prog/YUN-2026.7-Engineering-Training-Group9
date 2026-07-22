package com.medical.platform.dto;

import lombok.Data;

@Data
public class SymptomRequest {

    private String symptomName;

    private String symptomCode;

    private String severity;

    private String onsetTime;

    private String duration;

    private String location;

    private String description;

    private String associatedSymptoms;
}