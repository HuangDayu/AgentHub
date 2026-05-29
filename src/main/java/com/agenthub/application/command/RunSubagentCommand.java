package com.agenthub.application.command;

import com.agenthub.domain.model.agent.ReActAgentContext;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 子Agent运行命令。
 */
@Data
@NoArgsConstructor
public class RunSubagentCommand {
    private ReActAgentContext parentContext;
    private String name;
    private String systemPrompt;
    private String task;
    private List<String> tools;
    private String knowledgeIds;
    private String modelConfigId;
}
