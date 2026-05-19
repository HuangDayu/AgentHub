package com.agenthub.infrastructure.tools.skills_tools;

import com.agenthub.domain.model.tools.Skill;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * 技能工具回调，将Skill适配为Spring AI的ToolCallback。
 *
 * @author huangdayu
 */
@Slf4j
public class SkillToolCallback implements ToolCallback {

    private final Skill skill;
    private final ObjectMapper objectMapper;

    public SkillToolCallback(Skill skill, ObjectMapper objectMapper) {
        this.skill = skill;
        this.objectMapper = objectMapper;
    }

    public String getName() {
        return skill.getSkillCode();
    }

    @Override
    public String call(String functionInput) {
        log.info("Executing skill: {} with input: {}", skill.getSkillCode(), functionInput);
        try {
            return buildSuccessResponse(functionInput);
        } catch (Exception e) {
            log.error("Failed to execute skill: {}", skill.getSkillCode(), e);
            return buildErrorResponse(e);
        }
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return ToolDefinition.builder()
                .name(skill.getSkillCode())
                .description(skill.getDescription())
                .inputSchema(buildInputSchema())
                .build();
    }

    private String buildSuccessResponse(String functionInput) throws Exception {
        Map<String, Object> result = createResultMap();
        result.put("input", parseInput(functionInput));
        result.put("filesTree", parseFilesTree());
        return objectMapper.writeValueAsString(result);
    }

    private Map<String, Object> createResultMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("skill", skill.getSkillCode());
        result.put("name", skill.getName());
        result.put("description", skill.getDescription());
        return result;
    }

    private Object parseFilesTree() {
        try {
            return objectMapper.readValue(skill.getSkillFilesTree(), Map.class);
        } catch (Exception e) {
            return skill.getSkillFilesTree();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseInput(String functionInput) {
        try {
            if (functionInput == null || functionInput.isBlank()) return new HashMap<>();
            return objectMapper.readValue(functionInput, Map.class);
        } catch (Exception e) {
            return Map.of("raw", functionInput);
        }
    }

    private String buildInputSchema() {
        return "{\"type\":\"object\",\"properties\":{\"input\":{\"type\":\"string\",\"description\":\"技能输入参数\"}}}";
    }

    private String buildErrorResponse(Exception e) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return objectMapper.writeValueAsString(response);
        } catch (Exception ex) {
            return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
