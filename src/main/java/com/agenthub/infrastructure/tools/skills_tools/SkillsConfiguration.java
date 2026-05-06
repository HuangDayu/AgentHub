package com.agenthub.infrastructure.tools.skills_tools;

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
    



    /**
     * 技能根目录路径 Bean。
     */
    @Bean("skillsRootPath")
    public Path skillsRootPath() {
        return Path.of(skillsRootPath);
    }

}
