package com.agenthub.application.usecase;

import com.agenthub.application.port.out.agent.AgentPort;
import com.agenthub.application.port.out.repositories.AgentRepository;
import com.agenthub.application.port.out.repositories.StudioSessionRepository;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.domain.model.ChatMessage;
import com.agenthub.domain.model.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Agent对话用例，处理Agent对话和Session管理。
 * 
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class AgentChatUseCase {
    
    private final AgentPort agentPort;
    private final AgentRepository agentRepository;
    private final StudioSessionRepository sessionRepository;
    
    /**
     * 创建新会话。
     */
    public String createSession(String agentId) {
        validateAgentExists(agentId);
        Session session = Session.create(agentId, null, null);
        Session saved = sessionRepository.save(session);
        return saved.getId();
    }
    
    /**
     * 同步对话。
     */
    public String chat(String agentId, String sessionId, String userMessage) {
        validateSession(sessionId, agentId);
        AssistantMessage response = agentPort.call(agentId, userMessage);
        return saveMessages(sessionId, userMessage, response);
    }
    
    /**
     * 流式对话。
     */
    public Flux<Message> streamChat(String agentId, String sessionId, String userMessage) {
        validateSession(sessionId, agentId);
        saveUserMessage(sessionId, userMessage);
        StringBuilder responseBuilder = new StringBuilder();
        return agentPort.streamMessages(agentId, userMessage)
                .doOnNext(msg -> appendResponse(responseBuilder, msg))
                .doOnComplete(() -> saveAssistantMessage(sessionId, responseBuilder));
    }
    
    /**
     * 保存用户消息。
     */
    private void saveUserMessage(String sessionId, String userMessage) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            session.addUserMessage(userMessage);
            sessionRepository.addMessage(session);
        }
    }
    
    /**
     * 追加响应内容。
     */
    private void appendResponse(StringBuilder builder, Message message) {
        if (message instanceof AssistantMessage am) {
            builder.append(am.getText());
        }
    }
    
    /**
     * 保存助手消息。
     */
    private void saveAssistantMessage(String sessionId, StringBuilder builder) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null && builder.length() > 0) {
            session.addAssistantMessage(builder.toString());
            sessionRepository.addMessage(session);
        }
    }
    
    /**
     * 中断Agent执行。
     */
    public void interrupt(String agentId) {
        agentPort.interrupt(agentId);
    }
    
    /**
     * 验证Agent存在。
     */
    private void validateAgentExists(String agentId) {
        agentRepository.findById(agentId)
                .orElseThrow(() -> new NotFoundException("Agent not found: " + agentId));
    }
    
    /**
     * 验证会话归属。
     */
    private void validateSession(String sessionId, String agentId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));
        if (!session.getAgentId().equals(agentId)) {
            throw new NotFoundException("Session not owned by agent: " + agentId);
        }
    }
    
    /**
     * 保存消息到会话。
     */
    private String saveMessages(String sessionId, String userMessage, AssistantMessage response) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) return response.getText();
        
        session.addUserMessage(userMessage);
        session.addAssistantMessage(response.getText());
        sessionRepository.addMessage(session);
        return response.getText();
    }
}
