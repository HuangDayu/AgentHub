package com.agenthub;

import com.embabel.agent.spi.config.spring.AgentPlatformConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.agenthub")
@MapperScan("com.agenthub.infrastructure.persistence")
@SpringBootApplication(scanBasePackages = "com.agenthub", exclude = {
        AgentPlatformConfiguration.class
})
public class AgentHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentHubApplication.class, args);
    }
}
