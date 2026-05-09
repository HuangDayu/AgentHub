package com.agenthub.infrastructure.agents;

import com.agenthub.domain.model.ReActAgentContext;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

/**
 * @author huangdayu
 */
public abstract class AbstractReActAgent {

    public abstract String getName();

    public abstract ReActAgentContext getContext();

    public abstract Flux<Message> streamMessages(String userMessage);

    public abstract AssistantMessage call(String userMessage);

    public abstract void interrupt();

}
