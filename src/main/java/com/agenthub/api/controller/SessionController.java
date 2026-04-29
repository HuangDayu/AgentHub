package com.agenthub.api.controller;

import com.agenthub.api.dto.MessageResponse;
import com.agenthub.api.dto.SendMessageRequest;
import com.agenthub.api.dto.SessionResponse;
import com.agenthub.application.dto.MessageOutput;
import com.agenthub.application.dto.SessionOutput;
import com.agenthub.api.mapper.MessageResponseMapper;
import com.agenthub.application.usecase.ChatUseCase;
import com.agenthub.application.usecase.SessionUseCase;
import com.agenthub.domain.model.ChatMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 会话 API 控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions")
public class SessionController {
    private final SessionUseCase sessionUseCase;
    private final ChatUseCase agentChatUseCase;

    public SessionController(SessionUseCase sessionUseCase, ChatUseCase agentChatUseCase) {
        this.sessionUseCase = sessionUseCase;
        this.agentChatUseCase = agentChatUseCase;
    }


    /**
     * 将会话输出转换为响应DTO。
     *
     * @param output 会话输出DTO
     * @return 会话响应DTO
     */
    private static SessionResponse toResponse(SessionOutput output) {
        return new SessionResponse(
                output.id(),
                output.agentId(),
                output.createdAt()
        );
    }

    /**
     * 将消息输出转换为响应DTO。
     *
     * @param output 消息输出DTO
     * @return 消息响应DTO
     */
    private static MessageResponse toResponse(MessageOutput output) {
        return new MessageResponse(
                output.id(),
                output.sessionId(),
                output.role(),
                output.content(),
                output.createdAt()
        );
    }

    /**
     * 为智能体创建新会话。
     *
     * @param agentId 智能体ID
     * @return 创建的会话响应
     */
    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@PathVariable String agentId) {
        SessionOutput output = sessionUseCase.create(agentId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(output));
    }

    /**
     * 查询智能体的所有会话。
     *
     * @param agentId 智能体ID
     * @return 会话响应列表
     */
    @GetMapping
    public List<SessionResponse> listSessions(@PathVariable String agentId) {
        return sessionUseCase.list(agentId).stream()
                .map(SessionController::toResponse)
                .toList();
    }

    /**
     * 向会话发送消息并获取回复。
     *
     * @param agentId   智能体ID
     * @param sessionId 会话ID
     * @param request   消息请求体
     * @return 回复消息响应
     */
    @PostMapping("/{sessionId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable String agentId,
            @PathVariable String sessionId,
            @RequestBody SendMessageRequest request
    ) {
        MessageOutput output = agentChatUseCase.sendChat(agentId, sessionId, request.content());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(output));
    }

    /**
     * 获取会话的消息历史。
     *
     * @param agentId   智能体ID
     * @param sessionId 会话ID
     * @return 消息响应列表
     */
    @GetMapping("/{sessionId}/messages")
    public List<MessageResponse> listMessages(
            @PathVariable String agentId,
            @PathVariable String sessionId
    ) {
        List<ChatMessage> messages = sessionUseCase.list(agentId, sessionId);
        return messages.stream()
                .map(MessageResponseMapper::toResponse)
                .toList();
    }

    /**
     * 流式发送消息并返回SSE响应。
     *
     * @param agentId   智能体ID
     * @param sessionId 会话ID
     * @param request   消息请求体
     * @return SSE发射器
     */
    @PostMapping(value = "/{sessionId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessageStream(
            @PathVariable String agentId,
            @PathVariable String sessionId,
            @RequestBody SendMessageRequest request
    ) {
        return agentChatUseCase.streamChat(agentId, sessionId, request.content());
    }

    /**
     * 将ChatMessage转换为MessageResponse。
     */
}
