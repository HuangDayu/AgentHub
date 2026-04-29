package com.agenthub.application.usecase;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.dto.SessionOutput;
import com.agenthub.application.port.out.repositories.StudioSessionRepository;
import com.agenthub.domain.model.ChatMessage;
import com.agenthub.domain.model.Session;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author huangdayu
 */
@Component
public class SessionUseCase {

    private final StudioSessionRepository studioSessionRepository;
    private final AgentUseCase agentUseCase;

    public SessionUseCase(StudioSessionRepository studioSessionRepository, AgentUseCase agentUseCase) {
        this.studioSessionRepository = studioSessionRepository;
        this.agentUseCase = agentUseCase;
    }


    /**
     * 查询指定智能体的所有会话。
     *
     * @param agentId 智能体ID
     * @return 会话输出DTO列表
     */
    public List<SessionOutput> list(String agentId) {
        // Verify agent exists
        agentUseCase.get(agentId);
        return studioSessionRepository.findByAgentId(agentId).stream()
                .map(s -> new SessionOutput(s.getId(), s.getAgentId(), s.getCreatedAt()))
                .toList();
    }


    /**
     * 列出指定会话的所有消息。
     *
     * @param agentId   智能体ID
     * @param sessionId 会话ID
     * @return 消息列表
     */
    public List<ChatMessage> list(String agentId, String sessionId) {
        Session session = findAndValidateSession(agentId, sessionId);
        return session.getMessages();
    }

    /**
     * 查找并验证会话归属。
     */
    private Session findAndValidateSession(String agentId, String sessionId) {
        Session session = studioSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));
        if (!session.getAgentId().equals(agentId)) {
            throw new NotFoundException("Session not owned by agent: " + agentId);
        }
        return session;
    }


    /**
     * 为指定智能体创建新会话，先验证智能体存在。
     *
     * @param agentId 智能体ID
     * @return 创建的会话输出DTO
     */
    public SessionOutput create(String agentId) {
        // Verify agent exists
        agentUseCase.get(agentId);

        Session session = Session.create(agentId, null, null);
        Session saved = studioSessionRepository.save(session);
        return new SessionOutput(saved.getId(), saved.getAgentId(), saved.getCreatedAt());
    }

}
