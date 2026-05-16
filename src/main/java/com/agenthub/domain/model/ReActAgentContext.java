package com.agenthub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author huangdayu
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ReActAgentContext {

    private Agent agent;
    private String sessionId;
    private String chatModelId;
    private String systemPrompt;
    private List<AgentToolInfo> tools;
    private List<String> knowledgeIds;
    private ModelStrategy modelStrategy;
    private ToolStrategy toolStrategy;
    private GuardrailStrategy guardrailStrategy;
    private RetrievalStrategy retrievalStrategy;
    private List<AgentConfig> agentConfigs;
    private ReActAgentWorkspace workspace;

}
