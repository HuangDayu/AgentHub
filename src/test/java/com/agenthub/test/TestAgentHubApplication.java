package com.agenthub.test;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

/**
 * 测试用 Spring Boot 启动类。
 */
@SpringBootApplication(scanBasePackages = {"com.agenthub",})
@MapperScan("com.agenthub.infrastructure.persistence")
@ActiveProfiles("test")
public class TestAgentHubApplication {


    public static void main(String[] args) {
        SpringApplication.run(TestAgentHubApplication.class, args);
    }
}
