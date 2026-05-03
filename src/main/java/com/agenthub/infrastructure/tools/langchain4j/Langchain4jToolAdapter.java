package com.agenthub.infrastructure.tools.langchain4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.core.operator.tool_call.ToolRegistry;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring AI ToolDefinition 到 LangChain4j ToolSpecification 的适配器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Langchain4jToolAdapter {

    private final ToolCallbackProvider toolCallbackProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将所有 Spring AI 工具转换为 LangChain4j 工具规范
     */
    public List<ToolSpecification> convertToLangchain4jSpecs() {
        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(this::convertToSpec)
                .toList();
    }

    /**
     * 转换单个工具定义 - 使用 ToolSpecification.fromJson() 直接转换
     */
    private ToolSpecification convertToSpec(ToolCallback toolCallback) {
        ToolDefinition toolDefinition = toolCallback.getToolDefinition();
        String json = buildToolJson(toolDefinition);
        return ToolSpecification.fromJson(json);
    }

    /**
     * 构建 LangChain4j ToolSpecification 所需的 JSON 格式
     */
    private String buildToolJson(ToolDefinition toolDefinition) {
        try {
            Map<String, Object> toolMap = new HashMap<>();
            toolMap.put("name", toolDefinition.name());
            toolMap.put("description", toolDefinition.description());
            String inputSchema = toolDefinition.inputSchema();
            if (!inputSchema.isEmpty() && !inputSchema.equals("{}")) {
                Map<String, Object> parameters = objectMapper.readValue(inputSchema, new TypeReference<>() {});
                toolMap.put("parameters", parameters);
            }
            return objectMapper.writeValueAsString(toolMap);
        } catch (Exception e) {
            log.warn("Failed to build JSON for tool {}: {}", toolDefinition.name(), e.getMessage());
            return String.format("{\"name\":\"%s\",\"description\":\"%s\"}", toolDefinition.name(), toolDefinition.description());
        }
    }
}
