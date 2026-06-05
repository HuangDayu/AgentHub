package com.agenthub.test;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;

/**
 * 测试用 Spring Boot 启动类。
 */
@EnableAsync
@EnableScheduling
@ActiveProfiles("test")
@MapperScan("com.agenthub.infrastructure.store.db.mapper")
@SpringBootApplication(scanBasePackages = "com.agenthub")
public class TestAgentHubApplication {


    public static void main(String[] args) {
        SpringApplication.run(TestAgentHubApplication.class, args);
    }
}
