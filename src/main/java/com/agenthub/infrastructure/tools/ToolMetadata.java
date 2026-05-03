package com.agenthub.infrastructure.tools;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class ToolMetadata {
    
    private final String name;
    private final String description;
    private final String[] tags;
    private final boolean requiresAuth;
    private final String securityLevel;
    private final Method method;
    private final Object instance;
    private final List<ParameterMetadata> parameters;
    
    public ToolMetadata(String name, String description, String[] tags, 
                       boolean requiresAuth, String securityLevel,
                       Method method, Object instance, List<ParameterMetadata> params) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.requiresAuth = requiresAuth;
        this.securityLevel = securityLevel;
        this.method = method;
        this.instance = instance;
        this.parameters = params;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String[] getTags() { return tags; }
    public boolean isRequiresAuth() { return requiresAuth; }
    public String getSecurityLevel() { return securityLevel; }
    public Method getMethod() { return method; }
    public Object getInstance() { return instance; }
    public List<ParameterMetadata> getParameters() { return parameters; }
    
    public static class ParameterMetadata {
        private final String name;
        private final String description;
        private final boolean required;
        private final String defaultValue;
        private final Class<?> type;
        
        public ParameterMetadata(String name, String description, 
                                boolean required, String defaultValue, Class<?> type) {
            this.name = name;
            this.description = description;
            this.required = required;
            this.defaultValue = defaultValue;
            this.type = type;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public boolean isRequired() { return required; }
        public String getDefaultValue() { return defaultValue; }
        public Class<?> getType() { return type; }
    }
}
