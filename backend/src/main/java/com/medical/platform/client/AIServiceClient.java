package com.medical.platform.client;

import com.medical.platform.dto.AIAnalyzeRequest;
import com.medical.platform.dto.AIAnalyzeResponse;
import com.medical.platform.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * AI服务HTTP客户端
 *
 * 用于后端调用AI服务的分析接口
 */
@Slf4j
@Component
public class AIServiceClient {

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;

    /**
     * 构造函数，注入RestTemplate
     * 使用配置的超时时间，避免长时间阻塞
     */
    public AIServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 调用AI分析接口
     *
     * @param visitId 就诊ID
     * @param symptoms 症状名称列表
     * @param patientAge 患者年龄
     * @param durationDays 症状持续天数
     * @param additionalInfo 附加信息
     * @return AI分析响应
     */
    public AIAnalyzeResponse analyze(Long visitId, List<String> symptoms,
                                      Integer patientAge, Integer durationDays,
                                      String additionalInfo) {
        AIAnalyzeRequest request = new AIAnalyzeRequest();
        request.setVisitId(visitId);
        request.setSymptoms(symptoms);
        request.setPatientAge(patientAge);
        request.setDurationDays(durationDays);
        request.setAdditionalInfo(additionalInfo);

        String url = aiServiceUrl + "/api/ai/analyze";

        try {
            log.info("调用AI分析服务: visitId={}, symptomsCount={}", visitId, symptoms.size());
            AIAnalyzeResponse response = restTemplate.postForObject(url, request, AIAnalyzeResponse.class);

            if (response == null) {
                log.error("AI服务返回空响应: visitId={}", visitId);
                throw new BusinessException(500, "AI服务返回空响应");
            }

            log.info("AI分析完成: visitId={}, riskLevel={}", visitId, response.getRiskLevel());
            return response;

        } catch (RestClientException e) {
            log.error("调用AI服务失败: visitId={}, error={}", visitId, e.getMessage(), e);
            throw new BusinessException(503, "AI服务暂时不可用: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI分析未知错误: visitId={}", visitId, e);
            throw new BusinessException(500, "AI分析失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     *
     * @return AI服务是否可用
     */
    public boolean healthCheck() {
        String url = aiServiceUrl + "/api/ai/health";
        try {
            String response = restTemplate.getForObject(url, String.class);
            boolean isHealthy = response != null && response.contains("\"status\":\"ok\"");
            log.debug("AI服务健康检查: {}", isHealthy ? "正常" : "异常");
            return isHealthy;
        } catch (Exception e) {
            log.warn("AI服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }
}