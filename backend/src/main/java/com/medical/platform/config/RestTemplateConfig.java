package com.medical.platform.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate配置类
 * 用于配置HTTP客户端的超时时间、连接池等参数
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 配置RestTemplate Bean
     * 设置连接超时和读取超时，避免AI服务调用时长时间阻塞
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(10))  // 连接超时10秒
            .setReadTimeout(Duration.ofSeconds(60))      // 读取超时60秒（AI分析可能较慢）
            .build();
    }
}