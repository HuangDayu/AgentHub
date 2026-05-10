package com.agenthub.application.command;

import com.agenthub.domain.model.ModelStrategy;
import com.agenthub.domain.model.RetrievalStrategy;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagCommand {
    private String sessionId;
    private String agentId;
    private String prompt;
    private List<String> kbIds;
    private RetrievalStrategy strategy;
    private ModelStrategy modelStrategy;
    private String promptTemplate;
}
