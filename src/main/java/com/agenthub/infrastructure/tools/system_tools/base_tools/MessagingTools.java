package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.domain.model.RuntimeMessage;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.Instant;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@AgentTools(name = "MessagingTools", description = "消息工具，提供消息发送和管理功能")
public class MessagingTools {


    @Tool(description = "发送消息")
    public RuntimeMessage message(@ToolParam String sessionId, @ToolParam String content, @ToolParam String role) {
        RuntimeMessage message = new RuntimeMessage();
        message.setSessionId(sessionId);
        message.setContent(content);
        message.setRole(role);
        message.setCreatedAt(Instant.now());
        // TODO 发送到消息通道
        return message;
    }
}
