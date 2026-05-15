package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.ChatMessage;
import com.agenthub.domain.model.Session;
import com.agenthub.infrastructure.store.db.entity.ChatMessageEntity;
import com.agenthub.infrastructure.store.db.entity.SessionEntity;
import com.agenthub.infrastructure.store.db.mapper.ChatMessageMybatisMapper;
import com.agenthub.infrastructure.store.db.mapper.SessionMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 基于 MyBatis 的会话仓储实现。
 */
@Component
@Primary
public class MybatisSessionRepository implements SessionRepository {
    private final SessionMybatisMapper sessionMapper;
    private final ChatMessageMybatisMapper messageMapper;

    public MybatisSessionRepository(SessionMybatisMapper sessionMapper, ChatMessageMybatisMapper messageMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    @Transactional
    public Session save(Session session) {
        SessionEntity sessionEntity = toSessionPo(session);
        sessionMapper.insert(sessionEntity);
        return BeanUtil.copyProperties(sessionEntity, Session.class);
    }

    @Override
    public Optional<Session> findSessionMessageById(String sessionId) {
        SessionEntity sessionEntity = sessionMapper.selectById(sessionId);
        if (sessionEntity == null) {
            return Optional.empty();
        }

        LambdaQueryWrapper<ChatMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageEntity::getSessionId, sessionEntity.getId())
                .orderByAsc(ChatMessageEntity::getCreatedAt);
        List<ChatMessageEntity> chatMessageEntities = messageMapper.selectList(wrapper);
        Session session = toSession(sessionEntity, chatMessageEntities);
        return Optional.of(session);
    }

    @Override
    public List<Session> findByAgentId(String agentId) {
        LambdaQueryWrapper<SessionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SessionEntity::getAgentId, agentId)
                .orderByDesc(SessionEntity::getCreatedAt);
        List<SessionEntity> sessionEntities = sessionMapper.selectList(wrapper);
        return sessionEntities.stream().map(v -> BeanUtil.copyProperties(v, Session.class)).toList();
    }

    @Override
    public void saveMessages(List<ChatMessage> messages) {
        List<ChatMessageEntity> list = messages.stream().map(message -> toMessagePo(message, message.getSessionId())).toList();
        messageMapper.insert(list);
    }

    @Override
    public void delete(String sessionId) {
        sessionMapper.deleteById(sessionId);
        LambdaQueryWrapper<ChatMessageEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessageEntity::getSessionId, sessionId);
        messageMapper.delete(queryWrapper);
    }


    @Override
    public Session existSession(String sessionId, String agentId) {
        SessionEntity sessionEntity = sessionMapper.selectById(sessionId);
        if (sessionEntity == null || !sessionEntity.getAgentId().equals(agentId)) {
            throw new NotFoundException("Session not owned by agent: " + agentId);
        }
        return toSession(sessionEntity, new ArrayList<>());
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
        Session session = BeanUtil.copyProperties(sessionEntity, Session.class);
        List<ChatMessage> list = chatMessageEntities.stream().map(message -> BeanUtil.copyProperties(message, ChatMessage.class)).toList();
        session.setMessages(list);
        return session;
    }
}
