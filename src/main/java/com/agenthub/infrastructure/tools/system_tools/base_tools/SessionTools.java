package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.domain.model.agent.Session;
import com.agenthub.domain.model.agent.ChatMessage;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.Instant;
import java.util.List;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@AgentTools(name = "SessionTools", description = "会话工具，提供会话状态查询、历史记录、列表和消息发送功能")
public class SessionTools {

    private final SessionRepository sessionRepository;

    @Tool(description = "获取会话状态")
    public Session sessionStatus(@ToolParam String sessionId) {
        return sessionRepository.findSessionMessageById(sessionId).orElse(null);
    }

    @Tool(description = "获取会话历史消息")
    public List<ChatMessage> sessionsHistory(@ToolParam String sessionId) {
        return null;
    }

    @Tool(description = "获取智能体的所有会话列表")
    public List<Session> sessionsList(@ToolParam String agentId) {
        return sessionRepository.findByAgentId(agentId);
    }

    @Tool(description = "向会话发送消息")
    public ChatMessage sessionsSend(@ToolParam String sessionId, @ToolParam String content, @ToolParam String role) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setContent(content);
        message.setRole(role);
        message.setCreatedAt(Instant.now());
        // TODO 发送到消息通道
        return message;
    }
}
