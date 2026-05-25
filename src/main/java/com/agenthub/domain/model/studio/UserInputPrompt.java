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
     * 创建用户输入请求.
     */
    public static UserInputPrompt create(
        String requestId,
        String runId,
        String agentId,
        String agentName,
        String structuredInput
    ) {
        UserInputPrompt request = new UserInputPrompt();
        request.setRequestId(requestId);
        request.setRunId(runId);
        request.setAgentId(agentId);
        request.setAgentName(agentName);
        request.setStructuredInput(structuredInput);
        request.setCreatedAt(Instant.now());
        return request;
    }
}
