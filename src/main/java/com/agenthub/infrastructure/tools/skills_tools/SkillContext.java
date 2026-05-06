package com.agenthub.infrastructure.tools.skills_tools;

import java.util.HashMap;
import java.util.Map;

/**
 * 技能执行上下文。
 * 
 * @author huangdayu
 */
public class SkillContext {
    
    private final String sessionId;
    private final String agentId;
    private final String tenantId;
    private final String workspaceId;
    private final Map<String, Object> parameters;
    private final Map<String, Object> metadata;
    
    private SkillContext(Builder builder) {
        this.sessionId = builder.sessionId;
        this.agentId = builder.agentId;
        this.tenantId = builder.tenantId;
        this.workspaceId = builder.workspaceId;
        this.parameters = new HashMap<>(builder.parameters);
        this.metadata = new HashMap<>(builder.metadata);
    }
    
    public String getSessionId() { return sessionId; }
    public String getAgentId() { return agentId; }
    public String getTenantId() { return tenantId; }
    public String getWorkspaceId() { return workspaceId; }
    public Map<String, Object> getParameters() { return new HashMap<>(parameters); }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private String sessionId;
        private String agentId;
        private String tenantId;
        private String workspaceId;
        private Map<String, Object> parameters = new HashMap<>();
        private Map<String, Object> metadata = new HashMap<>();
        
        public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public Builder agentId(String agentId) { this.agentId = agentId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder workspaceId(String workspaceId) { this.workspaceId = workspaceId; return this; }
        
        public Builder parameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }
        
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }
        
        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public SkillContext build() { return new SkillContext(this); }
    }
}
