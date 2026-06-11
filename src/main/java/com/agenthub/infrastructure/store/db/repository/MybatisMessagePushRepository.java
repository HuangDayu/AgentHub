package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.MessagePushRepository;
import com.agenthub.domain.model.studio.MessagePush;
import com.agenthub.infrastructure.store.db.entity.MessagePushEntity;
import com.agenthub.infrastructure.store.db.mapper.MessagePushMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息推送仓储实现.
 */
@Component
@RequiredArgsConstructor
public class MybatisMessagePushRepository implements MessagePushRepository {

    private final MessagePushMapper mapper;

    @Override
    public MessagePush save(MessagePush messagePush) {
        MessagePushEntity entity = toEntity(messagePush);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public List<MessagePush> findByRunId(String runId) {
        LambdaQueryWrapper<MessagePushEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessagePushEntity::getRunId, runId);
        return mapper.selectList(wrapper).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public List<MessagePush> findAll() {
        return mapper.selectList(null).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private MessagePushEntity toEntity(MessagePush domain) {
        MessagePushEntity entity = new MessagePushEntity();
        entity.setId(domain.getMessageId());
        entity.setMessageId(domain.getMessageId());
        entity.setRunId(domain.getRunId());
        entity.setRole(domain.getRole());
        entity.setContent(domain.getContent());
        entity.setMetadata(domain.getMetadata());
        entity.setTimestamp(domain.getTimestamp());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private MessagePush toDomain(MessagePushEntity entity) {
        return BeanUtil.copyProperties(entity, MessagePush.class);
    }
}
