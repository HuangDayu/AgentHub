package com.agenthub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.agenthub.infrastructure.persistence")
@SpringBootApplication(scanBasePackages = "com.agenthub")
/** 微服务启动类 */
public class AgentHubApplication {
    /**
     * 应用入口
     */
    public static void main(String[] args) {
        SpringApplication.run(AgentHubApplication.class, args);
    }
}
