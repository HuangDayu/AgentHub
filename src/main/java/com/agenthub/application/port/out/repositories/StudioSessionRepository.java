package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.Session;

import java.util.List;
import java.util.Optional;

/**
 * 会话仓储接口，定义会话的持久化操作。
 */
public interface StudioSessionRepository {

    Session save(Session session);

    Optional<Session> findById(String sessionId);

    List<Session> findByAgentId(String agentId);

    void addMessage(Session session);
}
