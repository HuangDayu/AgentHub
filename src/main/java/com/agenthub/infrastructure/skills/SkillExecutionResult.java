package com.agenthub.infrastructure.skills;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 技能执行结果。
 * 
 * @author huangdayu
 */
public class SkillExecutionResult {
    
    private final String skillId;
    private final String skillCode;
    private final boolean success;
    private final Object output;
    private final String errorMessage;
    private final long executionTimeMs;
    private final Instant executedAt;
    private final Map<String, Object> metadata;
    
    private SkillExecutionResult(Builder builder) {
        this.skillId = builder.skillId;
        this.skillCode = builder.skillCode;
        this.success = builder.success;
        this.output = builder.output;
        this.errorMessage = builder.errorMessage;
        this.executionTimeMs = builder.executionTimeMs;
        this.executedAt = builder.executedAt;
        this.metadata = new HashMap<>(builder.metadata);
    }
    
    public String getSkillId() { return skillId; }
    public String getSkillCode() { return skillCode; }
    public boolean isSuccess() { return success; }
    public Object getOutput() { return output; }
    public String getErrorMessage() { return errorMessage; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public Instant getExecutedAt() { return executedAt; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private String skillId;
        private String skillCode;
        private boolean success;
        private Object output;
        private String errorMessage;
        private long executionTimeMs;
        private Instant executedAt;
        private Map<String, Object> metadata = new HashMap<>();
        
        public Builder skillId(String skillId) { this.skillId = skillId; return this; }
        public Builder skillCode(String skillCode) { this.skillCode = skillCode; return this; }
        public Builder success(boolean success) { this.success = success; return this; }
        public Builder output(Object output) { this.output = output; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder executionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; return this; }
        public Builder executedAt(Instant executedAt) { this.executedAt = executedAt; return this; }
        
        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public SkillExecutionResult build() { return new SkillExecutionResult(this); }
    }
}
