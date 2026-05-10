package com.agenthub.infrastructure.rag;

import com.agenthub.application.port.out.rag.RagModelChatPort;
import com.agenthub.domain.model.ChatMessage;
import com.agenthub.domain.model.SessionMessage;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class RagSpringModelChatAdapter implements RagModelChatPort {
    private static final Logger log = LoggerFactory.getLogger(RagSpringModelChatAdapter.class);
    private final SpringShareObjectFactory springShareObjectFactory;

    public RagSpringModelChatAdapter(SpringShareObjectFactory springShareObjectFactory) {
        this.springShareObjectFactory = springShareObjectFactory;
    }

    @Override
    public String chat(SessionMessage sessionMessage) {
        return chat(getChatClient(sessionMessage), sessionMessage.getChatMessages());
    }

    @Override
    public Flux<String> stream(SessionMessage sessionMessage) {
        return stream(getChatClient(sessionMessage), sessionMessage.getChatMessages());
    }

    public Flux<String> stream(ChatClient chatClient, List<ChatMessage> messages) {
        List<Message> promptMessages = buildPromptMessages(null, messages);
        Prompt prompt = new Prompt(promptMessages);
        return chatClient.prompt(prompt).stream().content();
    }

    public String chat(ChatClient chatClient, List<ChatMessage> messages) {
        List<Message> promptMessages = buildPromptMessages(null, messages);
        Prompt prompt = new Prompt(promptMessages);
        return chatClient.prompt(prompt).call().content();
    }

    private ChatClient getChatClient(SessionMessage sessionMessage) {
        return springShareObjectFactory.getChatClientBySessionId(sessionMessage);
    }

    private List<Message> buildPromptMessages(String systemPrompt, List<ChatMessage> messages) {
        List<Message> promptMessages = new java.util.ArrayList<>();
        addSystemPrompt(promptMessages, systemPrompt);
        for (ChatMessage msg : messages) addChatMessage(promptMessages, msg);
        return promptMessages;
    }

    private void addSystemPrompt(List<Message> promptMessages, String systemPrompt) {
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            promptMessages.add(new SystemMessage(systemPrompt));
        }
    }

    private void addChatMessage(List<Message> promptMessages, ChatMessage msg) {
        switch (msg.getRole()) {
            case "user" -> promptMessages.add(new UserMessage(msg.getContent()));
            case "system" -> promptMessages.add(new SystemMessage(msg.getContent()));
            case "assistant" -> promptMessages.add(new AssistantMessage(msg.getContent()));
            default -> promptMessages.add(new UserMessage(msg.getContent()));
        }
    }
}
