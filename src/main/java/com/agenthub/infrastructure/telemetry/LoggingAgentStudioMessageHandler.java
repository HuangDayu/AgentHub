package com.agenthub.infrastructure.telemetry;

import com.agenthub.application.port.out.repositories.MessagePushRepository;
import com.agenthub.application.port.out.repositories.RunRegistrationRepository;
import com.agenthub.application.port.out.repositories.UserInputRequestRepository;
import com.agenthub.domain.model.studio.MessagePush;
import com.agenthub.domain.model.studio.RunRegistration;
import com.agenthub.domain.model.studio.UserInputPrompt;
import io.agentscope.core.studio.pojo.PushMessageRequest;
import io.agentscope.core.studio.pojo.RegisterRunRequest;
import io.agentscope.core.studio.pojo.RequestUserInputRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static org.springframework.ai.util.json.JsonParser.toJson;

/**
 * Agent Studio消息处理器实现.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAgentStudioMessageHandler implements AgentStudioMessageHandler {

    private final RunRegistrationRepository runRegistrationRepository;
    private final MessagePushRepository messagePushRepository;
    private final UserInputRequestRepository userInputRequestRepository;

    @Override
    public void registerRun(RegisterRunRequest payload) {
        log.info("Register run: {}", toJson(payload));
        saveRunRegistration(payload);
    }

    @Override
    public void pushMessage(PushMessageRequest payload) {
        log.info("Push message: {}", toJson(payload));
        saveMessagePush(payload);
    }

    @Override
    public void requestUserInput(RequestUserInputRequest payload) {
        log.info("Request user input: {}", toJson(payload));
        saveUserInputPrompt(payload);
    }

    private void saveRunRegistration(RegisterRunRequest payload) {
        try {
            RunRegistration registration = RunRegistration.create(
                    payload.getId(),
                    payload.getProject(),
                    payload.getName(),
                    Instant.parse(payload.getTimestamp()),
                    Math.toIntExact(payload.getPid()),
                    payload.getStatus(),
                    payload.getRunDir()
            );
            runRegistrationRepository.save(registration);
        } catch (Exception e) {
            log.error("Failed to save run registration", e);
        }
    }

    private void saveMessagePush(PushMessageRequest payload) {
        try {
            MessagePush messagePush = MessagePush.create(
                    payload.getReplyId(),
                    payload.getRunId(),
                    payload.getRole(),
                    toJson(payload.getMsg()),
                    toJson(payload.getMsg().getMetadata()),
                    Instant.parse(payload.getMsg().getTimestamp())
            );
            messagePushRepository.save(messagePush);
        } catch (Exception e) {
            log.error("Failed to save message push", e);
        }
    }

    private void saveUserInputPrompt(RequestUserInputRequest payload) {
        try {
            UserInputPrompt request = UserInputPrompt.create(
                    payload.getRequestId(),
                    payload.getRunId(),
                    payload.getAgentId(),
                    payload.getAgentName(),
                    toJson(payload.getStructuredInput())
            );
            userInputRequestRepository.save(request);
        } catch (Exception e) {
            log.error("Failed to save user input request", e);
        }
    }
}
