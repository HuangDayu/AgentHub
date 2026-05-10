package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentTeamCommand {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String teamCode;
    private String name;
    private String description;
    private String coordinationMode;
    private String memberConfig;
    private String status;

}
