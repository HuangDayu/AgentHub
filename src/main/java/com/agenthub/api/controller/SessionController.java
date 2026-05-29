package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.*;
import com.agenthub.api.mapper.MessageResponseMapper;
import com.agenthub.application.command.AgentChatCommand;
import com.agenthub.application.command.SubAgentChatCommand;
import com.agenthub.application.command.SubsessionCommand;
import com.agenthub.application.dto.ChatAttachmentOutput;
import com.agenthub.application.dto.SessionOutput;
import com.agenthub.application.dto.SubsessionOutput;
import com.agenthub.application.usecase.AgentChatUseCase;
import com.agenthub.application.usecase.ChatAttachmentUseCase;
import com.agenthub.application.usecase.SessionUseCase;
import com.agenthub.application.usecase.SubsessionUseCase;
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
 * 会话 API 控制器，管理会话及其子会话（Subsession）。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/agents/{agentId}/sessions")
public class SessionController {
    private final SessionUseCase sessionUseCase;
    private final AgentChatUseCase agentChatUseCase;
    private final ChatAttachmentUseCase chatAttachmentUseCase;
    private final SubsessionUseCase subsessionUseCase;

    private static SessionResponse toResponse(SessionOutput output) {
        return BeanUtil.copyProperties(output, SessionResponse.class);
    }

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(
            @PathVariable String agentId,
            @RequestBody(required = false) CreateSessionRequest request) {
        String name = request != null ? request.getName() : null;
        SessionOutput output = sessionUseCase.create(agentId, name);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(output));
    }

    @GetMapping
    public List<SessionResponse> listSessions(@PathVariable String agentId) {
        return sessionUseCase.list(agentId).stream()
                .map(SessionController::toResponse).toList();
    }

    @PostMapping("/{sessionId}/messages")
    public AgentMessageResponse sendMessage(
            @PathVariable String agentId,
            @PathVariable String sessionId,
            @RequestBody SendMessageRequest request) {
        AgentMessage agentMessage = agentChatUseCase.chatMessages(new AgentChatCommand(agentId, sessionId, request.getContent(), request.getFilePaths()));
        return BeanUtil.copyProperties(agentMessage, AgentMessageResponse.class);
    }

    @GetMapping("/{sessionId}/messages")
    public List<MessageResponse> listMessages(
            @PathVariable String agentId,
            @PathVariable String sessionId) {
        List<ChatMessage> messages = sessionUseCase.list(agentId, sessionId);
        return messages.stream().map(MessageResponseMapper::toResponse).toList();
    }

    @PostMapping(value = "/{sessionId}/messages/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AgentMessageResponse> sendMessageStream(
            @PathVariable String agentId,
            @PathVariable String sessionId,
            @RequestBody SendMessageRequest request) {
        return agentChatUseCase.streamMessages(new AgentChatCommand(agentId, sessionId, request.getContent(), request.getFilePaths()))
                .map(v -> BeanUtil.copyProperties(v, AgentMessageResponse.class));
    }

    @PostMapping(value = "/{sessionId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ChatAttachmentResponse> uploadAttachments(
            @PathVariable String agentId,
            @PathVariable String sessionId,
            @RequestPart(name = "file", required = false) MultipartFile file,
            @RequestPart(name = "files", required = false) List<MultipartFile> files) {
        sessionUseCase.list(agentId, sessionId);
        return chatAttachmentUseCase.upload(sessionId, mergeFiles(file, files)).stream()
                .map(this::toAttachmentResponse).toList();
    }

    @DeleteMapping("/{sessionId}")
    public void deleteSession(@PathVariable String sessionId) {
        sessionUseCase.deleteSession(sessionId);
    }


    @PostMapping("/{sessionId}/subsessions")
    public ResponseEntity<SubsessionResponse> createSubsession(
            @PathVariable String sessionId,
            @PathVariable String agentId,
            @RequestBody SubsessionRequest request) {
        SubsessionCommand command = new SubsessionCommand(
                sessionId, request.getSubagentId(), request.getName());
        SubsessionOutput output = subsessionUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toSubsessionResponse(output));
    }

    @GetMapping("/{sessionId}/subsessions")
    public List<SubsessionResponse> listSubsessions(@PathVariable String sessionId) {
        return subsessionUseCase.listByParentSession(sessionId).stream()
                .map(SessionController::toSubsessionResponse).toList();
    }

    @GetMapping("/{sessionId}/subsessions/{subsessionId}")
    public SubsessionResponse getSubsession(@PathVariable String subsessionId) {
        return toSubsessionResponse(subsessionUseCase.get(subsessionId));
    }

    @PostMapping("/{sessionId}/subsessions/{subsessionId}/close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeSubsession(@PathVariable String subsessionId) {
        subsessionUseCase.close(subsessionId);
    }

    @GetMapping("/{sessionId}/subsessions/{subsessionId}/messages")
    public List<MessageResponse> listSubsessionMessages(
            @PathVariable String agentId,
            @PathVariable String subsessionId) {
        return subsessionUseCase.getMessages(subsessionId).stream()
                .map(MessageResponseMapper::toResponse)
                .toList();
    }

    @PostMapping(value = "/{sessionId}/subsessions/{subsessionId}/messages/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AgentMessageResponse> streamSubsessionMessage(
            @PathVariable String subsessionId,
            @RequestBody SendMessageRequest request) {
        return subsessionUseCase.streamMessage(new SubAgentChatCommand(null, subsessionId, request.getContent(), request.getFilePaths()))
                .map(v -> BeanUtil.copyProperties(v, AgentMessageResponse.class));
    }

    private List<MultipartFile> mergeFiles(MultipartFile file, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) return files;
        if (file != null) return List.of(file);
        return List.of();
    }

    private ChatAttachmentResponse toAttachmentResponse(ChatAttachmentOutput output) {
        return BeanUtil.copyProperties(output, ChatAttachmentResponse.class);
    }

    private static SubsessionResponse toSubsessionResponse(SubsessionOutput output) {
        return BeanUtil.copyProperties(output, SubsessionResponse.class);
    }
}
