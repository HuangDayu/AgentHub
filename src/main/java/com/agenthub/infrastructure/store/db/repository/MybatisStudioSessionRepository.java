package com.agenthub.infrastructure.store.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.StudioSessionRepository;
import com.agenthub.domain.model.ChatMessage;
import com.agenthub.domain.model.Session;
import com.agenthub.infrastructure.store.db.entity.ChatMessageEntity;
import com.agenthub.infrastructure.store.db.entity.SessionEntity;
import com.agenthub.infrastructure.store.db.mapper.ChatMessageMybatisMapper;
import com.agenthub.infrastructure.store.db.mapper.SessionMybatisMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 MyBatis 的会话仓储实现。
 */
@Component
@Primary
public class MybatisStudioSessionRepository implements StudioSessionRepository {
    private final SessionMybatisMapper sessionMapper;
    private final ChatMessageMybatisMapper messageMapper;

    public MybatisStudioSessionRepository(SessionMybatisMapper sessionMapper, ChatMessageMybatisMapper messageMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    @Transactional
    public Session save(Session session) {
        SessionEntity sessionEntity = toSessionPo(session);

        // 检查session是否已存在
        SessionEntity existingSession = sessionMapper.selectById(session.getId());
        if (existingSession == null) {
            // 新session，插入
            sessionMapper.insert(sessionEntity);
        }

        // 查询已存在的消息ID
        LambdaQueryWrapper<ChatMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageEntity::getSessionId, session.getId());
        List<ChatMessageEntity> existingMessages = messageMapper.selectList(wrapper);
        List<String> existingMessageIds = existingMessages.stream()
                .map(ChatMessageEntity::getId)
                .toList();

        // 只保存新消息
        List<ChatMessageEntity> chatMessageEntities = new ArrayList<>(existingMessages);
        for (ChatMessage message : session.getMessages()) {
            if (!existingMessageIds.contains(message.getId())) {
                ChatMessageEntity chatMessageEntity = toMessagePo(message, session.getId());
                messageMapper.insert(chatMessageEntity);
                chatMessageEntities.add(chatMessageEntity);
            }
        }
        return toSession(sessionEntity, chatMessageEntities);
    }

    @Override
    public Optional<Session> findById(String sessionId) {
        SessionEntity sessionEntity = sessionMapper.selectById(sessionId);
        if (sessionEntity == null) {
            return Optional.empty();
        }

        // 查询消息
        LambdaQueryWrapper<ChatMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageEntity::getSessionId, sessionId)
                .orderByAsc(ChatMessageEntity::getCreatedAt);
        List<ChatMessageEntity> chatMessageEntities = messageMapper.selectList(wrapper);

        Session session = toSession(sessionEntity, chatMessageEntities);
        return Optional.of(session);
    }

    @Override
    public List<Session> findByAgentId(String agentId) {
        LambdaQueryWrapper<SessionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SessionEntity::getAgentId, agentId)
                .orderByAsc(SessionEntity::getCreatedAt);
        List<SessionEntity> sessionEntities = sessionMapper.selectList(wrapper);

        return sessionEntities.stream().map(sessionEntity -> {
            LambdaQueryWrapper<ChatMessageEntity> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.eq(ChatMessageEntity::getSessionId, sessionEntity.getId())
                    .orderByAsc(ChatMessageEntity::getCreatedAt);
            List<ChatMessageEntity> chatMessageEntities = messageMapper.selectList(msgWrapper);
            return toSession(sessionEntity, chatMessageEntities);
        }).collect(Collectors.toList());
    }

    @Override
    public void addMessage(Session session) {
        List<ChatMessage> messages = session.getMessages();
        if (messages != null && !messages.isEmpty()) {
            Set<ChatMessageEntity> messageSet = messages.stream().map(message -> toMessagePo(message, session.getId())).collect(Collectors.toSet());
            messageMapper.insert(messageSet);
        }
    }

    private SessionEntity toSessionPo(Session session) {
        SessionEntity po = new SessionEntity();
        po.setId(session.getId());
        po.setAgentId(session.getAgentId());
        po.setTenantId(session.getTenantId());
        po.setWorkspaceId(session.getWorkspaceId());
        po.setCreatedAt(session.getCreatedAt());
        return po;
    }

    private ChatMessageEntity toMessagePo(ChatMessage message, String sessionId) {
        ChatMessageEntity po = new ChatMessageEntity();
        po.setId(message.getId());
        po.setSessionId(sessionId);
        po.setRole(message.getRole());
        po.setContent(message.getContent());
        po.setCreatedAt(message.getCreatedAt());
        return po;
    }

    private Session toSession(SessionEntity sessionEntity, List<ChatMessageEntity> chatMessageEntities) {
        Session session = new Session(sessionEntity.getId(), sessionEntity.getAgentId(), sessionEntity.getTenantId(), sessionEntity.getWorkspaceId(), sessionEntity.getCreatedAt());
        for (ChatMessageEntity chatMessageEntity : chatMessageEntities) {
            if ("user".equals(chatMessageEntity.getRole())) {
                session.addUserMessage(chatMessageEntity.getContent());
            } else {
                session.addAssistantMessage(chatMessageEntity.getContent());
            }
        }
        return session;
    }
}
