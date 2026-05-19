package com.agenthub.application.port.out.rag;

import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.rag.RagChatMessage;
import reactor.core.publisher.Flux;

/**
 * 聊天服务端口。
 */
public interface RagModelChatPort {

    /**
     * 发送聊天消息。
     */
    AgentMessage chat(RagChatMessage ragChatMessage);


    /**
     * 发送聊天消息流式响应
     *
     * @return
     */
    Flux<AgentMessage> stream(RagChatMessage ragChatMessage);
}
