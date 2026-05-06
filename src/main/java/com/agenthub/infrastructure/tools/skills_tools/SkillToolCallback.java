package com.agenthub.infrastructure.tools.skills_tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * 技能工具回调，将AgentSkill适配为Spring AI的ToolCallback。
 * 
 * @author huangdayu
 */
@Slf4j
public class SkillToolCallback implements ToolCallback {
    
    private final SkillManifest manifest;
    private final SkillExecutor skillExecutor;
    private final ObjectMapper objectMapper;
    
    public SkillToolCallback(SkillManifest manifest, SkillExecutor skillExecutor, 
                           ObjectMapper objectMapper) {
        this.manifest = manifest;
        this.skillExecutor = skillExecutor;
        this.objectMapper = objectMapper;
    }
    
    public String getName() {
        return manifest.getCode();
    }
    
    @Override
    public ToolDefinition getToolDefinition() {
        return ToolDefinition.builder()
                .name(manifest.getCode())
                .description(manifest.getDescription())
                .inputSchema(buildInputSchema())
                .build();
    }
    
    @Override
    public String call(String functionInput) {
        log.info("Calling skill: {} with input: {}", manifest.getCode(), functionInput);
        
        try {
            return executeSkill(functionInput);
        } catch (Exception e) {
            log.error("Skill execution failed: {}", manifest.getCode(), e);
            return buildErrorResponse(e);
        }
    }
    
    /**
     * 执行技能。
     */
    private String executeSkill(String functionInput) throws Exception {
        SkillContext context = buildContext(functionInput);
        SkillExecutionResult result = skillExecutor.executeByCode(manifest.getCode(), context);
        
        if (result.isSuccess()) {
            return buildSuccessResponse(result);
        }
        return buildFailureResponse(result);
    }
    
    /**
     * 构建执行上下文。
     */
    private SkillContext buildContext(String functionInput) throws Exception {
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> params = objectMapper.readValue(functionInput, 
                java.util.Map.class);
        
        return SkillContext.builder()
                .parameters(params)
                .build();
    }
    
    /**
     * 构建输入Schema。
     */
    private String buildInputSchema() {
        if (manifest.getParameters() == null || manifest.getParameters().isEmpty()) {
            return "{}";
        }
        
        try {
            return objectMapper.writeValueAsString(manifest.getParameters());
        } catch (Exception e) {
            log.warn("Failed to build input schema", e);
            return "{}";
        }
    }
    
    /**
     * 构建成功响应。
     */
    private String buildSuccessResponse(SkillExecutionResult result) {
        try {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("output", result.getOutput());
            response.put("executionTimeMs", result.getExecutionTimeMs());
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"success\":true,\"output\":\"" + result.getOutput() + "\"}";
        }
    }
    
    /**
     * 构建失败响应。
     */
    private String buildFailureResponse(SkillExecutionResult result) {
        try {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("error", result.getErrorMessage());
            response.put("executionTimeMs", result.getExecutionTimeMs());
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"success\":false,\"error\":\"" + result.getErrorMessage() + "\"}";
        }
    }
    
    /**
     * 构建错误响应。
     */
    private String buildErrorResponse(Exception e) {
        try {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return objectMapper.writeValueAsString(response);
        } catch (Exception ex) {
            return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
