package com.agenthub.application.usecase;

import com.agenthub.application.dto.SessionOutput;
import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.application.port.out.repositories.SubsessionRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.agent.ChatMessage;
import com.agenthub.domain.model.agent.Session;
import com.agenthub.domain.model.agent.Subsession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class SessionUseCase {

    private final SessionRepository sessionRepository;
    private final SubsessionRepository subsessionRepository;
    private final AgentUseCase agentUseCase;


    /**
     * 查询指定智能体的所有会话。
     *
     * @param agentId 智能体ID
     * @return 会话输出DTO列表
     */
    public List<SessionOutput> list(String agentId) {
        // Verify agent exists
        agentUseCase.get(agentId);
        return sessionRepository.findByAgentId(agentId).stream()
                .map(s -> new SessionOutput(s.getId(), s.getAgentId(), s.getName(), s.getCreatedAt()))
                .toList();
    }


    /**
     * 列出指定会话或子会话的所有消息。
     *
     * @param agentId   智能体ID
     * @param sessionId 会话ID（可为Session或Subsession ID）
     * @return 消息列表
     */
    public List<ChatMessage> list(String agentId, String sessionId) {
        // 先尝试作为Session加载
        var sessionOpt = sessionRepository.findSessionMessageById(sessionId);
        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            if (!session.getAgentId().equals(agentId)) {
                throw new NotFoundException("Session not owned by agent: " + agentId);
            }
            return session.getMessages();
        }
        // 回退：作为Subsession加载
        return subsessionRepository.findByIdWithMessages(sessionId)
                .map(Subsession::getMessages)
                .orElseThrow(() -> new NotFoundException("Session/Subsession not found: " + sessionId));
    }


    /**
     * 为指定智能体创建新会话，先验证智能体存在。
     *
     * @param agentId 智能体ID
     * @param name    会话名称
     * @return 创建的会话输出DTO
     */
    public SessionOutput create(String agentId, String name) {
        agentUseCase.get(agentId);
        Session session = Session.create(agentId, name, null, null);
        Session saved = sessionRepository.save(session);
        return new SessionOutput(saved.getId(), saved.getAgentId(), saved.getName(), saved.getCreatedAt());
    }

    public void deleteSession(String sessionId) {
        sessionRepository.delete(sessionId);
    }
}
