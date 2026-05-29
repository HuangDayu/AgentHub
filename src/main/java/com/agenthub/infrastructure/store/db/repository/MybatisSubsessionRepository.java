package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.SubsessionRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.agent.ChatMessage;
import com.agenthub.domain.model.agent.Subsession;
import com.agenthub.infrastructure.store.db.entity.ChatMessageEntity;
import com.agenthub.infrastructure.store.db.entity.SubsessionEntity;
import com.agenthub.infrastructure.store.db.mapper.ChatMessageMybatisMapper;
import com.agenthub.infrastructure.store.db.mapper.SubsessionMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 基于MyBatis的子会话仓储实现。
 */
@Component
@Primary
public class MybatisSubsessionRepository implements SubsessionRepository {
    private final SubsessionMybatisMapper subsessionMapper;
    private final ChatMessageMybatisMapper messageMapper;

    public MybatisSubsessionRepository(SubsessionMybatisMapper subsessionMapper,
                                        ChatMessageMybatisMapper messageMapper) {
        this.subsessionMapper = subsessionMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    public Subsession save(Subsession subsession) {
        SubsessionEntity entity = toEntity(subsession);
        subsessionMapper.insertOrUpdate(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Subsession> findById(String id) {
        return Optional.ofNullable(subsessionMapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<Subsession> findByParentSessionId(String parentSessionId) {
        LambdaQueryWrapper<SubsessionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubsessionEntity::getParentSessionId, parentSessionId);
        return subsessionMapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Subsession> findBySubagentId(String subagentId) {
        LambdaQueryWrapper<SubsessionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubsessionEntity::getSubagentId, subagentId);
        return subsessionMapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void saveMessage(ChatMessage message) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setId(message.getId());
        entity.setSessionId(message.getSessionId());
        entity.setRole(message.getRole());
        entity.setContent(message.getContent());
        entity.setCreatedAt(message.getCreatedAt());
        messageMapper.insert(entity);
    }

    @Override
    public Optional<Subsession> findByIdWithMessages(String id) {
        SubsessionEntity entity = subsessionMapper.selectById(id);
        if (entity == null) return Optional.empty();
        LambdaQueryWrapper<ChatMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageEntity::getSessionId, id)
                .orderByAsc(ChatMessageEntity::getCreatedAt);
        List<ChatMessage> messages = messageMapper.selectList(wrapper).stream()
                .map(m -> new ChatMessage(m.getId(), m.getSessionId(),
                        m.getRole(), m.getContent(), m.getCreatedAt()))
                .toList();
        Subsession subsession = toDomain(entity);
        subsession.setMessages(new ArrayList<>(messages));
        return Optional.of(subsession);
    }

    @Override
    public void deleteById(String id) {
        subsessionMapper.deleteById(id);
        LambdaQueryWrapper<ChatMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageEntity::getSessionId, id);
        messageMapper.delete(wrapper);
    }

    private SubsessionEntity toEntity(Subsession subsession) {
        if (subsession == null) return null;
        SubsessionEntity entity = new SubsessionEntity();
        entity.setId(subsession.getId());
        entity.setParentSessionId(subsession.getParentSessionId());
        entity.setSubagentId(subsession.getSubagentId());
        entity.setName(subsession.getName());
        entity.setStatus(subsession.getStatus());
        entity.setCreatedAt(subsession.getCreatedAt());
        entity.setUpdatedAt(subsession.getUpdatedAt());
        return entity;
    }

    private Subsession toDomain(SubsessionEntity entity) {
        if (entity == null) return null;
        Subsession subsession = new Subsession();
        subsession.setId(entity.getId());
        subsession.setParentSessionId(entity.getParentSessionId());
        subsession.setSubagentId(entity.getSubagentId());
        subsession.setName(entity.getName());
        subsession.setStatus(entity.getStatus());
        subsession.setCreatedAt(entity.getCreatedAt());
        subsession.setUpdatedAt(entity.getUpdatedAt());
        subsession.setMessages(new ArrayList<>());
        return subsession;
    }
}
