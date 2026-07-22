package com.medical.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 基层医疗安全型预问诊与随访平台 - 主启动类
 * 
 * @author 成员B
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.medical.platform.repository")
public class MedicalPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalPlatformApplication.class, args);
        System.out.println("医疗平台后端服务启动成功！");
        System.out.println("API 文档地址: http://localhost:8080/doc.html");
    }
}