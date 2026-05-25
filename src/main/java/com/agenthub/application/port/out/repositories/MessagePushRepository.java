package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.studio.MessagePush;

import java.util.List;

/**
 * 消息推送仓储接口.
 */
public interface MessagePushRepository {
    MessagePush save(MessagePush messagePush);
    List<MessagePush> findByRunId(String runId);
    List<MessagePush> findAll();
    void deleteById(String id);
}
