package com.agenthub.infrastructure.skills;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * 技能执行器，负责执行技能并返回执行结果。
 * 
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillExecutor {
    
    private final SkillFileManager skillFileManager;
    private final ObjectMapper objectMapper;
    
    /**
     * 根据技能代码执行技能。
     */
    public SkillExecutionResult executeByCode(String skillCode, SkillContext context) {
        return skillFileManager.getSkill(skillCode)
                .map(skill -> executeSkill(skill, context))
                .orElseGet(() -> createNotFoundResult(skillCode));
    }
    
    /**
     * 执行技能。
     */
    private SkillExecutionResult executeSkill(SkillPackage skillPackage, SkillContext context) {
        if (!isExecutable(skillPackage)) {
            return createNotExecutableResult(skillPackage);
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Executing skill: {}", skillPackage.getName());
            Object output = doExecute(skillPackage, context);
            return createSuccessResult(skillPackage, output, startTime);
        } catch (Exception e) {
            return createErrorResult(skillPackage, e, startTime);
        }
    }
    
    /**
     * 判断是否可执行。
     */
    private boolean isExecutable(SkillPackage skillPackage) {
        return skillPackage.isInstalled() && skillPackage.isValid();
    }
    
    /**
     * 执行实际逻辑。
     */
    private Object doExecute(SkillPackage skillPackage, SkillContext context) throws Exception {
        String type = skillPackage.getManifest().getType();
        
        return switch (type) {
            case "PROMPT" -> executePrompt(skillPackage, context);
            case "SCRIPT" -> executeScript(skillPackage, context);
            case "HTTP" -> executeHttp(skillPackage, context);
            case "WORKFLOW" -> executeWorkflow(skillPackage, context);
            default -> throw new UnsupportedOperationException("Unsupported skill type: " + type);
        };
    }
    
    /**
     * 执行提示词技能。
     */
    private Object executePrompt(SkillPackage skillPackage, SkillContext context) {
        String definition = skillPackage.getManifest().getDefinition();
        if (definition == null || definition.isBlank()) {
            throw new IllegalArgumentException("Prompt definition is empty");
        }
        
        return replaceParameters(definition, context.getParameters());
    }
    
    /**
     * 替换参数。
     */
    private String replaceParameters(String template, Map<String, Object> params) {
        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            result = result.replace(placeholder, String.valueOf(entry.getValue()));
        }
        return result;
    }
    
    /**
     * 执行脚本技能。
     */
    private Object executeScript(SkillPackage skillPackage, SkillContext context) {
        log.warn("Script skill not implemented: {}", skillPackage.getName());
        throw new UnsupportedOperationException("Script skill not implemented");
    }
    
    /**
     * 执行HTTP技能。
     */
    private Object executeHttp(SkillPackage skillPackage, SkillContext context) {
        log.warn("HTTP skill not implemented: {}", skillPackage.getName());
        throw new UnsupportedOperationException("HTTP skill not implemented");
    }
    
    /**
     * 执行工作流技能。
     */
    private Object executeWorkflow(SkillPackage skillPackage, SkillContext context) {
        log.warn("Workflow skill not implemented: {}", skillPackage.getName());
        throw new UnsupportedOperationException("Workflow skill not implemented");
    }
    
    /**
     * 创建未找到结果。
     */
    private SkillExecutionResult createNotFoundResult(String skillCode) {
        return SkillExecutionResult.builder()
                .success(false)
                .skillCode(skillCode)
                .errorMessage("Skill not found: " + skillCode)
                .executedAt(Instant.now())
                .build();
    }
    
    /**
     * 创建不可执行结果。
     */
    private SkillExecutionResult createNotExecutableResult(SkillPackage skillPackage) {
        return SkillExecutionResult.builder()
                .success(false)
                .skillId(skillPackage.getManifest().getId())
                .skillCode(skillPackage.getManifest().getCode())
                .errorMessage("Skill is not executable")
                .executedAt(Instant.now())
                .build();
    }
    
    /**
     * 创建成功结果。
     */
    private SkillExecutionResult createSuccessResult(SkillPackage skillPackage, 
                                                    Object output, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;
        log.info("Skill executed successfully: {}, time: {}ms", 
                skillPackage.getName(), executionTime);
        
        return SkillExecutionResult.builder()
                .success(true)
                .skillId(skillPackage.getManifest().getId())
                .skillCode(skillPackage.getManifest().getCode())
                .output(output)
                .executionTimeMs(executionTime)
                .executedAt(Instant.now())
                .build();
    }
    
    /**
     * 创建错误结果。
     */
    private SkillExecutionResult createErrorResult(SkillPackage skillPackage, 
                                                  Exception e, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;
        log.error("Skill execution failed: {}", skillPackage.getName(), e);
        
        return SkillExecutionResult.builder()
                .success(false)
                .skillId(skillPackage.getManifest().getId())
                .skillCode(skillPackage.getManifest().getCode())
                .errorMessage(e.getMessage())
                .executionTimeMs(executionTime)
                .executedAt(Instant.now())
                .build();
    }
}
