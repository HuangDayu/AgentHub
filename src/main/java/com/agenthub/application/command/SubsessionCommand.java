package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 子会话命令，用于创建子会话。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubsessionCommand {
    private String parentSessionId;
    private String subagentId;
    private String name;
}
