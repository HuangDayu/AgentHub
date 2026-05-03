package com.agenthub.infrastructure.tools;

import com.agenthub.infrastructure.tools.annotations.AgentTool;
import com.agenthub.infrastructure.tools.annotations.ToolParameter;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ToolRegistry {
    
    private final Map<String, ToolMetadata> tools = new ConcurrentHashMap<>();
    private final Map<String, List<ToolMetadata>> toolsByTag = new ConcurrentHashMap<>();
    private ToolSecurityPolicy securityPolicy;
    
    public void setSecurityPolicy(ToolSecurityPolicy policy) {
        this.securityPolicy = policy;
    }
    
    public void registerTools(Object toolProvider) {
        Class<?> clazz = toolProvider.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            AgentTool annotation = method.getAnnotation(AgentTool.class);
            if (annotation != null) {
                registerTool(toolProvider, method, annotation);
            }
        }
    }
    
    private void registerTool(Object instance, Method method, AgentTool annotation) {
        String name = annotation.name().isEmpty() ? method.getName() : annotation.name();
        List<ToolMetadata.ParameterMetadata> params = extractParameters(method);
        ToolMetadata metadata = new ToolMetadata(name, annotation.description(), 
            annotation.tags(), annotation.requiresAuth(), annotation.securityLevel(),
            method, instance, params);
        tools.put(name, metadata);
        for (String tag : annotation.tags()) {
            toolsByTag.computeIfAbsent(tag, k -> new ArrayList<>()).add(metadata);
        }
    }
    
    private List<ToolMetadata.ParameterMetadata> extractParameters(Method method) {
        List<ToolMetadata.ParameterMetadata> params = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            ToolParameter annotation = param.getAnnotation(ToolParameter.class);
            if (annotation != null) {
                params.add(new ToolMetadata.ParameterMetadata(
                    annotation.name().isEmpty() ? param.getName() : annotation.name(),
                    annotation.description(), annotation.required(), 
                    annotation.defaultValue(), param.getType()
                ));
            }
        }
        return params;
    }
    
    public Object execute(String toolName, String agentId, Map<String, Object> params) throws Exception {
        ToolMetadata metadata = tools.get(toolName);
        if (metadata == null) {
            throw new IllegalArgumentException("Tool not found: " + toolName);
        }
        if (securityPolicy != null) {
            securityPolicy.validateExecution(toolName, agentId, params);
        }
        Object[] args = buildArguments(metadata, params);
        return metadata.getMethod().invoke(metadata.getInstance(), args);
    }
    
    private Object[] buildArguments(ToolMetadata metadata, Map<String, Object> params) {
        List<ToolMetadata.ParameterMetadata> paramDefs = metadata.getParameters();
        Object[] args = new Object[paramDefs.size()];
        for (int i = 0; i < paramDefs.size(); i++) {
            ToolMetadata.ParameterMetadata paramDef = paramDefs.get(i);
            args[i] = params.getOrDefault(paramDef.getName(), paramDef.getDefaultValue());
        }
        return args;
    }
    
    public Optional<ToolMetadata> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }
    
    public Collection<ToolMetadata> getAllTools() {
        return Collections.unmodifiableCollection(tools.values());
    }
    
    public List<ToolMetadata> getToolsByTag(String tag) {
        return toolsByTag.getOrDefault(tag, Collections.emptyList());
    }
}
