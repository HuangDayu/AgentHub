package com.agenthub.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.db.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息数据映射器。
 */
@Mapper
public interface ChatMessageMybatisMapper extends BaseMapper<ChatMessageEntity> {
}

