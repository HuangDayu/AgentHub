package com.agenthub.infrastructure.tools.skills_tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 技能模块配置类。
 *
 * @author huangdayu
 */
@Configuration
public class SkillsConfiguration {

    @Bean
    public ObjectMapper skillObjectMapper() {
        return new ObjectMapper();
    }
}
