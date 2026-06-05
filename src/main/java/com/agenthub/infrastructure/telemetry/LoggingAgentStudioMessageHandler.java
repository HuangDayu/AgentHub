package com.agenthub.infrastructure.telemetry;

import cn.hutool.core.date.LocalDateTimeUtil;
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
        saveRunRegistration(payload);
    }

    @Override
    public void pushMessage(PushMessageRequest payload) {
        saveMessagePush(payload);
    }

    @Override
    public void requestUserInput(RequestUserInputRequest payload) {
        saveUserInputPrompt(payload);
    }

    private void saveRunRegistration(RegisterRunRequest payload) {
        try {
            RunRegistration.CreationSpec request = new RunRegistration.CreationSpec(
                    payload.getId(),
                    payload.getProject(),
                    payload.getName(),
                    LocalDateTimeUtil.parse(payload.getTimestamp(), "yyyy-MM-dd HH:mm:ss.SSS").toInstant(java.time.ZoneOffset.ofHours(8)),
                    Math.toIntExact(payload.getPid()),
                    payload.getStatus(),
                    payload.getRunDir()
            );
            RunRegistration registration = RunRegistration.create(request);
            runRegistrationRepository.save(registration);
        } catch (Exception e) {
            log.error("Failed to save run registration", e);
        }
    }

    private void saveMessagePush(PushMessageRequest payload) {
        try {
            MessagePush messagePush = MessagePush.create(buildMessagePushSpec(payload));
            messagePushRepository.save(messagePush);
        } catch (Exception e) {
            log.error("Failed to save message push", e);
        }
    }

    private MessagePush.CreationSpec buildMessagePushSpec(PushMessageRequest payload) {
        return new MessagePush.CreationSpec(
                payload.getReplyId(),
                payload.getRunId(),
                payload.getRole(),
                toJson(payload.getMsg()),
                toJson(payload.getMsg().getMetadata()),
                parseTimestamp(payload.getMsg().getTimestamp()));
    }

    private Instant parseTimestamp(String timestamp) {
        return LocalDateTimeUtil.parse(timestamp, "yyyy-MM-dd HH:mm:ss.SSS")
                .toInstant(java.time.ZoneOffset.ofHours(8));
    }

    private void saveUserInputPrompt(RequestUserInputRequest payload) {
        try {
            UserInputPrompt prompt = UserInputPrompt.create(buildUserInputSpec(payload));
            userInputRequestRepository.save(prompt);
        } catch (Exception e) {
            log.error("Failed to save user input request", e);
        }
    }

    private UserInputPrompt.CreationSpec buildUserInputSpec(RequestUserInputRequest payload) {
        return new UserInputPrompt.CreationSpec(
                payload.getRequestId(),
                payload.getRunId(),
                payload.getAgentId(),
                payload.getAgentName(),
                toJson(payload.getStructuredInput()));
    }
}
