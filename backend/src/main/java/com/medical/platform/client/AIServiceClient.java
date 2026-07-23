package com.medical.platform.client;

import com.medical.platform.dto.AIAnalyzeRequest;
import com.medical.platform.dto.AIAnalyzeResponse;
import com.medical.platform.dto.LLMRequest;
import com.medical.platform.dto.LLMResponse;
import com.medical.platform.dto.RetrieveRequest;
import com.medical.platform.dto.RetrieveResponse;
import com.medical.platform.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

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
     * 调用RAG检索接口
     *
     * @param query 查询文本
     * @param visitId 就诊ID
     * @param topK 返回结果数量
     * @return RAG检索响应
     */
    public RetrieveResponse retrieve(String query, Long visitId, Integer topK) {
        RetrieveRequest request = new RetrieveRequest();
        request.setQuery(query);
        request.setVisitId(visitId);
        request.setTopK(topK != null ? topK : 5);

        String url = aiServiceUrl + "/api/ai/retrieve";

        try {
            log.info("调用RAG检索服务: visitId={}, queryLength={}", visitId, query.length());
            RetrieveResponse response = restTemplate.postForObject(url, request, RetrieveResponse.class);

            if (response == null || !response.getSuccess()) {
                log.error("RAG检索失败: visitId={}", visitId);
                throw new BusinessException(500, "RAG检索失败");
            }

            log.info("RAG检索完成: visitId={}, totalResults={}", visitId, response.getTotal());
            return response;

        } catch (RestClientException e) {
            log.error("调用RAG检索服务失败: visitId={}, error={}", visitId, e.getMessage(), e);
            throw new BusinessException(503, "RAG检索服务暂时不可用: " + e.getMessage());
        } catch (Exception e) {
            log.error("RAG检索未知错误: visitId={}", visitId, e);
            throw new BusinessException(500, "RAG检索失败: " + e.getMessage());
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

    /**
     * 调用LLM推理接口
     *
     * @param promptTemplate Prompt模板名称
     * @param variables 模板变量值
     * @return LLM推理响应
     */
    public LLMResponse generateText(String promptTemplate, Map<String, Object> variables) {
        return generateText(promptTemplate, variables, 2048, 0.1f, false);
    }

    /**
     * 调用LLM推理接口（完整参数）
     *
     * @param promptTemplate Prompt模板名称
     * @param variables 模板变量值
     * @param maxTokens 最大生成token数
     * @param temperature 温度参数
     * @param stream 是否流式输出
     * @return LLM推理响应
     */
    public LLMResponse generateText(String promptTemplate, Map<String, Object> variables,
                                    Integer maxTokens, Float temperature, Boolean stream) {
        LLMRequest request = LLMRequest.builder()
                .promptTemplate(promptTemplate)
                .variables(variables)
                .maxTokens(maxTokens != null ? maxTokens : 2048)
                .temperature(temperature != null ? temperature : 0.1f)
                .stream(stream != null ? stream : false)
                .build();

        String url = aiServiceUrl + "/api/ai/generate";

        try {
            log.info("调用LLM推理服务: template={}, variablesCount={}",
                    promptTemplate, variables != null ? variables.size() : 0);
            LLMResponse response = restTemplate.postForObject(url, request, LLMResponse.class);

            if (response == null || !response.getSuccess()) {
                log.error("LLM推理失败: template={}", promptTemplate);
                throw new BusinessException(500, "LLM推理失败");
            }

            log.info("LLM推理完成: template={}, model={}", promptTemplate, response.getModel());
            return response;

        } catch (RestClientException e) {
            log.error("调用LLM推理服务失败: template={}, error={}", promptTemplate, e.getMessage(), e);
            throw new BusinessException(503, "LLM推理服务暂时不可用: " + e.getMessage());
        } catch (Exception e) {
            log.error("LLM推理未知错误: template={}", promptTemplate, e);
            throw new BusinessException(500, "LLM推理失败: " + e.getMessage());
        }
    }

    /**
     * 获取Prompt模板列表
     *
     * @return 模板列表响应
     */
    public String getTemplates() {
        String url = aiServiceUrl + "/api/ai/templates";
        try {
            String response = restTemplate.getForObject(url, String.class);
            log.debug("获取Prompt模板列表成功");
            return response;
        } catch (Exception e) {
            log.warn("获取Prompt模板列表失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取模型信息
     *
     * @return 模型信息响应
     */
    public String getModelInfo() {
        String url = aiServiceUrl + "/api/ai/model/info";
        try {
            String response = restTemplate.getForObject(url, String.class);
            log.debug("获取模型信息成功");
            return response;
        } catch (Exception e) {
            log.warn("获取模型信息失败: {}", e.getMessage());
            return null;
        }
    }
}