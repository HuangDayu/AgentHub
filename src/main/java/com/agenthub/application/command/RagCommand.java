package com.agenthub.application.command;

import com.agenthub.domain.model.ModelStrategy;
import com.agenthub.domain.model.RetrievalStrategy;

import java.util.List;

/**
 * 检索查询。Agent 调用知识库检索时传入的参数。
 */
public record RagCommand(
        String sessionId, String agentId, String prompt,
        List<String> kbIds, RetrievalStrategy strategy,
        ModelStrategy modelStrategy, String promptTemplate
) {
}
