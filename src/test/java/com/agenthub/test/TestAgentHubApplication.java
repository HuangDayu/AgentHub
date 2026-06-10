package com.agenthub.test;

import cn.hutool.core.io.FileUtil;
import com.agenthub.infrastructure.auth.JwtTokenProvider;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;

import static com.agenthub.test.common.TestCommonTools.TENANT_ID;
import static com.agenthub.test.common.TestCommonTools.TOKEN_FILE;

/**
 * 测试用 Spring Boot 启动类。
 */
@EnableAsync
@EnableScheduling
@ActiveProfiles({"test", "test-h2"})
@MapperScan("com.agenthub.infrastructure.store.db.mapper")
@SpringBootApplication(scanBasePackages = "com.agenthub")
public class TestAgentHubApplication {


    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(TestAgentHubApplication.class, args);
        writeToken(ctx);
    }

    private static void writeToken(ConfigurableApplicationContext ctx) {
        try {
            JwtTokenProvider tokenProvider = ctx.getBean(JwtTokenProvider.class);
            String token = tokenProvider.generateAccessToken("lisi", TENANT_ID);
            FileUtil.writeUtf8String("Bearer " + token, TOKEN_FILE);
        } catch (Exception ignored) {
        }
    }
}
