package com.agenthub.application.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionOutput {
    private /** 会话ID */String id;
    private /** 智能体ID */String agentId;
    private /** 会话名称 */String name;
    private /** 创建时间 */Instant createdAt;
}
