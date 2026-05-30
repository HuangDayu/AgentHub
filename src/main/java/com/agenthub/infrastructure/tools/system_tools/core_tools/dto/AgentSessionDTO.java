package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent会话DTO，仅暴露Agent决策所需的会话基本信息，不含消息内容。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentSessionDTO {
    private String id;
    private String name;
}
