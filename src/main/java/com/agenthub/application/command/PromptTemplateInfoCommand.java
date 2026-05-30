package com.agenthub.application.command;

import com.agenthub.application.dto.VariableOutput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateInfoCommand {

    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private String category;
    private String content;
    private List<VariableOutput> variables;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

}
