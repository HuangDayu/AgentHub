package com.agenthub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.agenthub.infrastructure.store")
@SpringBootApplication(scanBasePackages = "com.agenthub")
public class AgentHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentHubApplication.class, args);
    }
}
