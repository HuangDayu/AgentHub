package com.agenthub.infrastructure.tools.skills_tools;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 技能清单，描述技能的元数据信息。
 * <p>
 * 每个技能包必须包含一个 skill.json 或 skill.yaml 文件，
 * 用于描述技能的基本信息、依赖、参数等。
 * 
 * @author huangdayu
 */
@Data
public class SkillManifest {
    
    /**
     * 技能唯一标识符
     */
    private String id;
    
    /**
     * 技能代码（用于引用）
     */
    private String code;
    
    /**
     * 技能名称
     */
    private String name;
    
    /**
     * 技能描述
     */
    private String description;
    
    /**
     * 技能版本
     */
    private String version;
    
    /**
     * 技能类型
     */
    private String type;
    
    /**
     * 技能作者
     */
    private String author;
    
    /**
     * 技能主页
     */
    private String homepage;
    
    /**
     * 技能许可证
     */
    private String license;
    
    /**
     * 技能标签
     */
    private List<String> tags;
    
    /**
     * 技能参数定义
     */
    private Map<String, ParameterDefinition> parameters;
    
    /**
     * 技能依赖
     */
    private List<Dependency> dependencies;
    
    /**
     * 技能入口文件
     */
    private String entrypoint;
    
    /**
     * 技能配置
     */
    private Map<String, Object> config;
    
    /**
     * 创建时间
     */
    private Instant createdAt;
    
    /**
     * 更新时间
     */
    private Instant updatedAt;

    /**
     * 安装时间
     */
    private Instant installedAt;

    /**
     * 技能定义（用于Prompt类型技能）
     */
    private String definition;

    /**
     * 参数定义
     */
    @Data
    public static class ParameterDefinition {
        private String type;
        private String description;
        private boolean required;
        private Object defaultValue;
        private Map<String, Object> validation;
    }
    
    /**
     * 依赖定义
     */
    @Data
    public static class Dependency {
        private String name;
        private String version;
        private String type;
    }
}
