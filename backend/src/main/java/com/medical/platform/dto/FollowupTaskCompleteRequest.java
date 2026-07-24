package com.medical.platform.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 随访任务完成请求DTO
 */
@Data
public class FollowupTaskCompleteRequest {

    private Long executorId;

    private String result;

    private String notes;
}