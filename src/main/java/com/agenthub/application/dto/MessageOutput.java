package com.agenthub.application.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageOutput {
    private /** 消息ID */String id;
    private /** 会话ID */String sessionId;
    private /** 角色 */String role;
    private /** 内容 */String content;
    private /** 创建时间 */Instant createdAt;
}
