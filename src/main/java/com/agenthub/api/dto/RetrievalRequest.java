package com.agenthub.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * 检索请求。Agent 调用知识库检索时传入的参数。
 */
public record RetrievalRequest(
    String tenantId,
    String workspaceId,
    List<String> knowledgeBaseIds,
    String query,
    int topK,
    double scoreThreshold,
    boolean enableQueryRewrite,
    boolean enableRerank
) {}
