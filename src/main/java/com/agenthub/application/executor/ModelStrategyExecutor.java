package com.agenthub.application.executor;

import com.agenthub.application.port.out.rag.ModelChatPort;
import com.agenthub.domain.model.SessionMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ModelStrategyExecutor {
    private final ModelChatPort modelChatPort;

    public ModelStrategyExecutor(ModelChatPort modelChatPort) {
        this.modelChatPort = modelChatPort;
    }

    public String execute(SessionMessage sessionMessage) {
        return modelChatPort.chat(sessionMessage);
    }

    public Flux<String> stream(SessionMessage sessionMessage) {
        return modelChatPort.stream(sessionMessage);
    }
}
