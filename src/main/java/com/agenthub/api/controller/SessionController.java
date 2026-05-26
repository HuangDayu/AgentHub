package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.*;
import com.agenthub.api.mapper.MessageResponseMapper;
import com.agenthub.application.dto.ChatAttachmentOutput;
import com.agenthub.application.dto.SessionOutput;
import com.agenthub.application.usecase.AgentChatUseCase;
import com.agenthub.application.usecase.ChatAttachmentUseCase;
import com.agenthub.application.usecase.SessionUseCase;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 会话 API 控制器。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions")
public class SessionController {
    private final SessionUseCase sessionUseCase;
    private final AgentChatUseCase agentChatUseCase;
    private final ChatAttachmentUseCase chatAttachmentUseCase;


    /**
     * 将会话输出转换为响应DTO。
     *
     * @param output 会话输出DTO
     * @return 会话响应DTO
     */
    private static SessionResponse toResponse(SessionOutput output) {
        return BeanUtil.copyProperties(output, SessionResponse.class);
    }

    /**
     * 为智能体创建新会话。
     *
     * @param agentId 智能体ID
     * @param request 创建会话请求（可选）
     * @return 创建的会话响应
     */
    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@PathVariable String agentId, @RequestBody(required = false) CreateSessionRequest request) {
        String name = request != null ? request.getName() : null;
        SessionOutput output = sessionUseCase.create(agentId, name);
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
    public AgentMessageResponse sendMessage(
            @PathVariable String agentId,
            @PathVariable String sessionId,
            @RequestBody SendMessageRequest request
    ) {
        AgentMessage agentMessage = agentChatUseCase.chatMessages(agentId, sessionId, request.getContent(), request.getFilePaths());
        return BeanUtil.copyProperties(agentMessage, AgentMessageResponse.class);
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
    public Flux<AgentMessageResponse> sendMessageStream(
            @PathVariable String agentId,
            @PathVariable String sessionId,
            @RequestBody SendMessageRequest request
    ) {
        return agentChatUseCase.streamMessages(agentId, sessionId, request.getContent(), request.getFilePaths())
                .map(v -> BeanUtil.copyProperties(v, AgentMessageResponse.class));
    }

    @PostMapping(value = "/{sessionId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ChatAttachmentResponse> uploadAttachments(
            @PathVariable String agentId,
            @PathVariable String sessionId,
            @RequestPart(name = "file", required = false) MultipartFile file,
            @RequestPart(name = "files", required = false) List<MultipartFile> files
    ) {
        sessionUseCase.list(agentId, sessionId);
        return chatAttachmentUseCase.upload(sessionId, mergeFiles(file, files)).stream()
                .map(this::toAttachmentResponse)
                .toList();
    }

    private List<MultipartFile> mergeFiles(MultipartFile file, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) return files;
        if (file != null) return List.of(file);
        return List.of();
    }

    private ChatAttachmentResponse toAttachmentResponse(ChatAttachmentOutput output) {
        return BeanUtil.copyProperties(output, ChatAttachmentResponse.class);
    }

    @DeleteMapping("/{sessionId}")
    public void deleteSession(@PathVariable String sessionId) {
        sessionUseCase.deleteSession(sessionId);
    }

}
