package com.agenthub.domain.model.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DagWorkflowChat {
    private String agentId;
    private String sessionId;
    private String message;
}
