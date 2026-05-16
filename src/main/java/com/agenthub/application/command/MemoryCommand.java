package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 记忆聚合根，管理Agent的长期记忆存储。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoryCommand {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String agentId;
    private String name;
    private String memoryType;
    private String content;
    private String metadata;
    private double importance;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

}
