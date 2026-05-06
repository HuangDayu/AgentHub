package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息数据映射器。
 */
@Mapper
public interface ChatMessageMybatisMapper extends BaseMapper<ChatMessageEntity> {
}

