package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.agent.ChatMessage;
import com.agenthub.domain.model.agent.Subsession;

import java.util.List;
import java.util.Optional;

/**
 * 子会话仓储接口，定义子会话的持久化操作。
 */
public interface SubsessionRepository {

    /**
     * 保存子会话。
     *
     * @param subsession 子会话领域模型
     * @return 保存后的子会话
     */
    Subsession save(Subsession subsession);

    /**
     * 根据ID查找子会话。
     *
     * @param id 子会话ID
     * @return 可选子会话
     */
    Optional<Subsession> findById(String id);

    /**
     * 根据父会话ID查找所有子会话。
     *
     * @param parentSessionId 父会话ID
     * @return 子会话列表
     */
    List<Subsession> findByParentSessionId(String parentSessionId);

    /**
     * 根据子Agent ID查找子会话。
     *
     * @param subagentId 子Agent ID
     * @return 子会话列表
     */
    List<Subsession> findBySubagentId(String subagentId);

    /**
     * 保存子会话中的消息。
     *
     * @param message 聊天消息
     */
    void saveMessage(ChatMessage message);

    /**
     * 查找子会话及其消息。
     *
     * @param id 子会话ID
     * @return 可选子会话（含消息）
     */
    Optional<Subsession> findByIdWithMessages(String id);

    /**
     * 删除子会话。
     *
     * @param id 子会话ID
     */
    void deleteById(String id);
}
