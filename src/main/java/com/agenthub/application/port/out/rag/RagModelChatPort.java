package com.agenthub.application.port.out.rag;

import com.agenthub.domain.model.SessionMessage;
import reactor.core.publisher.Flux;

/**
 * 聊天服务端口。
 */
public interface RagModelChatPort {

    /**
     * 发送聊天消息。
     */
    String chat(SessionMessage sessionMessage);


    /**
     * 发送聊天消息流式响应
     *
     * @return
     */
    Flux<String> stream(SessionMessage sessionMessage);
}
