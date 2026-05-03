package com.agenthub.infrastructure.tools.langchain4j;

import com.agenthub.infrastructure.tools.ToolMetadata;
import com.agenthub.infrastructure.tools.ToolRegistry;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
public class Langchain4jToolAdapter {
    
    public List<ToolSpecification> convertToLangchain4jSpecs(ToolRegistry registry) {
        return registry.getAllTools().stream()
            .map(this::convertToSpec)
            .toList();
    }
    
    private ToolSpecification convertToSpec(ToolMetadata metadata) {
        return ToolSpecification.builder()
            .name(metadata.getName())
            .description(metadata.getDescription())
            .build();
    }
    
    public Object executeFromLangchain4j(ToolRegistry registry, 
                                        ToolExecutionRequest request, 
                                        String agentId) throws Exception {
        Map<String, Object> params = parseArguments(request.arguments());
        return registry.execute(request.name(), agentId, params);
    }
    
    private Map<String, Object> parseArguments(String arguments) {
        Map<String, Object> params = new HashMap<>();
        return params;
    }
}
