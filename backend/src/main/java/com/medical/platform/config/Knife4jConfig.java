package com.medical.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API 文档配置
 * 
 * @author 成员B
 */
@Configuration
public class Knife4jConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("医疗平台 API 文档")
                        .version("1.0.0")
                        .description("基层医疗安全型预问诊与随访平台 - 后端 API 文档")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("617013757duck-prog@users.noreply.github.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}