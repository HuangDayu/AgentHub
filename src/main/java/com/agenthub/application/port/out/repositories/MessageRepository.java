package com.agenthub.application.port.out.repositories;


import com.agenthub.domain.model.RuntimeMessage;

import java.util.List;

/**
 * 消息仓储接口。
 */
public interface MessageRepository {
    RuntimeMessage save(RuntimeMessage message);

    List<RuntimeMessage> findBySessionId(String sessionId);

    /**
     * 删除指定会话的所有消息。
     */
    void deleteBySessionId(String sessionId);
}