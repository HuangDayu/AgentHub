package com.agenthub.domain.model.studio;

import lombok.Data;

import java.time.Instant;

/**
 * 用户输入请求领域模型.
 */
@Data
public class UserInputPrompt {
    private String requestId;
    private String runId;
    private String agentId;
    private String agentName;
    private String structuredInput;
    private Instant createdAt;

    /**
     * 工厂方法所需字段快照。
     */
    public static final class CreationSpec {
        private final String requestId;
        private final String runId;
        private final String agentId;
        private final String agentName;
        private final String structuredInput;

        public CreationSpec(String requestId, String runId, String agentId,
                               String agentName, String structuredInput) {
            this.requestId = requestId;
            this.runId = runId;
            this.agentId = agentId;
            this.agentName = agentName;
            this.structuredInput = structuredInput;
        }
    }

    /**
     * 创建用户输入请求.
     */
    public static UserInputPrompt create(CreationSpec spec) {
        UserInputPrompt prompt = new UserInputPrompt();
        prompt.requestId = spec.requestId;
        prompt.runId = spec.runId;
        prompt.agentId = spec.agentId;
        prompt.agentName = spec.agentName;
        prompt.structuredInput = spec.structuredInput;
        prompt.createdAt = Instant.now();
        return prompt;
    }
}
