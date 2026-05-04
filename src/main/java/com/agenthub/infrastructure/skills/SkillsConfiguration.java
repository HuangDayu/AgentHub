package com.agenthub.infrastructure.skills;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * 技能模块配置类。
 * <p>
 * 配置技能文件管理相关的Bean和参数，以及与Spring AI的集成。
 * 
 * @author huangdayu
 */
@Configuration
public class SkillsConfiguration {
    
    @Value("${agenthub.skills.root-path:${user.home}/.agenthub/skills}")
    private String skillsRootPath;
    
    @Value("${agenthub.skills.auto-register:true}")
    private boolean autoRegisterSkills;
    
    /**
     * 配置 ObjectMapper 用于解析 JSON 格式的技能清单。
     */
    @Bean("skill.jsonObjectMapper")
    public ObjectMapper jsonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
    
    /**
     * 配置 ObjectMapper 用于解析 YAML 格式的技能清单。
     */
    @Bean("skill.yamlObjectMapper")
    public ObjectMapper yamlObjectMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        return mapper;
    }
    
    /**
     * 技能根目录路径 Bean。
     */
    @Bean("skill.rootPath")
    public Path skillsRootPath() {
        return Path.of(skillsRootPath);
    }
    
    /**
     * 注册技能工具回调提供者到Spring AI。
     */
    @Bean
    public ToolCallbackProvider skillToolCallbackProvider(
            SkillToolCallbackProvider skillToolCallbackProvider,
            SkillFileManager skillFileManager) {
        
        if (autoRegisterSkills) {
            skillFileManager.initialize();
        }
        
        return skillToolCallbackProvider;
    }
}
