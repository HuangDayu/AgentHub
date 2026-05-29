package com.agenthub.domain.model.agent;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 子会话聚合根，表示父会话内与特定子Agent的对话子会话。
 * 一个Session可以包含多个Subsession，每个Subsession对应一个Subagent。
 */
@Data
public class Subsession {
    private String id;
    private String parentSessionId;
    private String subagentId;
    private String name;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ChatMessage> messages;

    public Subsession() {
        this.id = randomId();
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.messages = new ArrayList<>();
    }

    /**
     * 创建新的子会话实例。
     *
     * @param parentSessionId 父会话ID
     * @param subagentId      子Agent ID
     * @param name            子会话名称
     * @return 新创建的子会话对象
     */
    public static Subsession create(String parentSessionId, String subagentId, String name) {
        Subsession subsession = new Subsession();
        subsession.parentSessionId = parentSessionId;
        subsession.subagentId = subagentId;
        subsession.name = name;
        return subsession;
    }

    /**
     * 关闭子会话。
     */
    public void close() {
        this.status = "CLOSED";
        this.updatedAt = Instant.now();
    }

    /**
     * 添加消息到子会话。
     *
     * @param message 聊天消息
     */
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
    }
}
